package com.ihsanguldur.coreledger.infrastructure.persistence;

import com.ihsanguldur.coreledger.domain.Account;
import com.ihsanguldur.coreledger.domain.entity.LedgerEntry;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import com.ihsanguldur.coreledger.domain.valueobject.Money;
import com.ihsanguldur.coreledger.domain.valueobject.TransactionId;
import com.ihsanguldur.coreledger.infrastructure.persistence.jpaentity.LedgerEntryJpaEntity;
import com.ihsanguldur.coreledger.infrastructure.persistence.repository.AccountJpaRepository;
import com.ihsanguldur.coreledger.infrastructure.persistence.repository.LedgerEntryJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
class AccountRepositoryAdapterIT {

    private static final Currency USD = Currency.getInstance("USD");

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private AccountJpaRepository accountJpaRepository;

    @Autowired
    private LedgerEntryJpaRepository ledgerEntryJpaRepository;

    @Autowired
    private TestEntityManager entityManager;

    private AccountRepositoryAdapter accountRepository;

    @BeforeEach
    void setUp() {
        accountRepository = new AccountRepositoryAdapter(accountJpaRepository, ledgerEntryJpaRepository);
    }

    @Test
    void saveThenFindByIdReturnsCorrectAggregate() {
        AccountId accountId = AccountId.generate();
        Account account = Account.open(accountId, USD);
        account.credit(Money.of(new BigDecimal("100.00"), USD), TransactionId.generate());

        accountRepository.save(account);

        Optional<Account> found = accountRepository.findById(accountId);

        assertThat(found).isPresent();
        assertThat(found.get().getAccountId()).isEqualTo(accountId);
        assertThat(found.get().getBalance()).isEqualTo(Money.of(new BigDecimal("100.00"), USD));
    }

    @Test
    void saveWritesLedgerEntryForEachDomainEvent() {
        AccountId accountId = AccountId.generate();
        Account account = Account.open(accountId, USD);
        TransactionId transactionId = TransactionId.generate();
        account.credit(Money.of(new BigDecimal("50.00"), USD), transactionId);

        accountRepository.save(account);

        List<LedgerEntryJpaEntity> entries = ledgerEntryJpaRepository.findAll();

        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).getAccountId()).isEqualTo(accountId.value());
        assertThat(entries.get(0).getDirection()).isEqualTo(LedgerEntry.Direction.CREDIT);
        assertThat(entries.get(0).getTransactionId()).isEqualTo(transactionId.value());
    }

    @Test
    void findByIdReturnsEmptyWhenAccountDoesNotExist() {
        assertThat(accountRepository.findById(AccountId.generate())).isEmpty();
    }

    @Test
    void concurrentSaveOnSameAccountThrowsOptimisticLockException() {
        AccountId accountId = AccountId.generate();
        Account account = Account.open(accountId, USD);
        accountRepository.save(account);
        entityManager.flush();
        entityManager.clear();

        Account firstRead = accountRepository.findById(accountId).orElseThrow();
        entityManager.clear();
        Account secondRead = accountRepository.findById(accountId).orElseThrow();

        firstRead.credit(Money.of(new BigDecimal("10.00"), USD), TransactionId.generate());
        accountRepository.save(firstRead);
        entityManager.flush();
        entityManager.clear();

        secondRead.credit(Money.of(new BigDecimal("20.00"), USD), TransactionId.generate());

        assertThatThrownBy(() -> {
            accountRepository.save(secondRead);
            entityManager.flush();
        }).isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    @Test
    void savingSameAccountInstanceTwiceDoesNotDuplicateLedgerEntries() {
        AccountId accountId = AccountId.generate();
        Account account = Account.open(accountId, USD);
        account.credit(Money.of(new BigDecimal("50.00"), USD), TransactionId.generate());

        accountRepository.save(account);
        accountRepository.save(account);

        List<LedgerEntryJpaEntity> entries = ledgerEntryJpaRepository.findAll();

        assertThat(entries).hasSize(1);
    }
}