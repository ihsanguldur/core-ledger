package com.ihsanguldur.coreledger.infrastructure.reconciliation;

import com.ihsanguldur.coreledger.domain.entity.LedgerEntry;
import com.ihsanguldur.coreledger.infrastructure.persistence.jpaentity.AccountJpaEntity;
import com.ihsanguldur.coreledger.infrastructure.persistence.jpaentity.LedgerEntryJpaEntity;
import com.ihsanguldur.coreledger.infrastructure.persistence.repository.AccountJpaRepository;
import com.ihsanguldur.coreledger.infrastructure.persistence.repository.LedgerEntryJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
class LedgerReconciliationJobIT {

    private static final String USD = "USD";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private AccountJpaRepository accountJpaRepository;

    @Autowired
    private LedgerEntryJpaRepository ledgerEntryJpaRepository;

    private LedgerReconciliationJob job;

    @BeforeEach
    void setUp() {
        job = new LedgerReconciliationJob(accountJpaRepository, ledgerEntryJpaRepository);
    }

    @Test
    void reportsNoDiscrepancyWhenProjectionMatchesLedger() {
        UUID accountId = UUID.randomUUID();
        saveAccount(accountId, new BigDecimal("100.0000"));
        saveEntry(accountId, LedgerEntry.Direction.CREDIT, new BigDecimal("150.0000"));
        saveEntry(accountId, LedgerEntry.Direction.DEBIT, new BigDecimal("50.0000"));

        assertThat(job.reconcile()).isEmpty();
    }

    @Test
    void treatsAccountWithNoLedgerEntriesAsZeroBalance() {
        saveAccount(UUID.randomUUID(), BigDecimal.ZERO);

        assertThat(job.reconcile()).isEmpty();
    }

    @Test
    void catchesAccountWhoseProjectionDivergesFromLedger() {
        UUID consistentId = UUID.randomUUID();
        saveAccount(consistentId, new BigDecimal("30.0000"));
        saveEntry(consistentId, LedgerEntry.Direction.CREDIT, new BigDecimal("30.0000"));

        UUID brokenId = UUID.randomUUID();
        saveAccount(brokenId, new BigDecimal("100.0000"));
        saveEntry(brokenId, LedgerEntry.Direction.CREDIT, new BigDecimal("100.0000"));
        saveEntry(brokenId, LedgerEntry.Direction.CREDIT, new BigDecimal("50.0000")); // rogue: ledger now 150, projection still 100

        List<LedgerReconciliationJob.BalanceDiscrepancy> discrepancies = job.reconcile();

        assertThat(discrepancies).hasSize(1);
        LedgerReconciliationJob.BalanceDiscrepancy d = discrepancies.get(0);
        assertThat(d.accountId()).isEqualTo(brokenId);
        assertThat(d.projectedBalance()).isEqualByComparingTo("100.00");
        assertThat(d.ledgerBalance()).isEqualByComparingTo("150.00");
        assertThat(d.difference()).isEqualByComparingTo("-50.00");
    }

    private void saveAccount(UUID accountId, BigDecimal balance) {
        AccountJpaEntity account = new AccountJpaEntity();
        account.setAccountId(accountId);
        account.setCurrency(USD);
        account.setBalance(balance);
        accountJpaRepository.save(account);
    }

    private void saveEntry(UUID accountId, LedgerEntry.Direction direction, BigDecimal amount) {
        LedgerEntryJpaEntity entry = new LedgerEntryJpaEntity();
        entry.setEntryId(UUID.randomUUID());
        entry.setAccountId(accountId);
        entry.setDirection(direction);
        entry.setAmount(amount);
        entry.setCurrency(USD);
        entry.setTransactionId(UUID.randomUUID());
        entry.setCreatedAt(Instant.now());
        ledgerEntryJpaRepository.save(entry);
    }
}
