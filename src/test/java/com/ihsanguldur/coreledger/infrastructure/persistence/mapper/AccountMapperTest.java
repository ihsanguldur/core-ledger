package com.ihsanguldur.coreledger.infrastructure.persistence.mapper;

import com.ihsanguldur.coreledger.domain.Account;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import com.ihsanguldur.coreledger.domain.valueobject.Money;
import com.ihsanguldur.coreledger.domain.valueobject.TransactionId;
import com.ihsanguldur.coreledger.infrastructure.persistence.jpaentity.AccountJpaEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

class AccountMapperTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void roundTripPreservesAccountState() {
        Account original = Account.open(AccountId.generate(), USD);
        original.credit(Money.of(new BigDecimal("150.00"), USD), TransactionId.generate());

        AccountJpaEntity entity = AccountMapper.toJpaEntity(original);
        Account reconstructed = AccountMapper.toDomain(entity);

        assertThat(reconstructed.getAccountId()).isEqualTo(original.getAccountId());
        assertThat(reconstructed.getBalance()).isEqualTo(original.getBalance());
        assertThat(reconstructed.getVersion()).isEqualTo(original.getVersion());
    }

    @Test
    void toJpaEntityMapsFieldsCorrectly() {
        Account account = Account.open(AccountId.generate(), USD);

        AccountJpaEntity entity = AccountMapper.toJpaEntity(account);

        assertThat(entity.getAccountId()).isEqualTo(account.getAccountId().value());
        assertThat(entity.getCurrency()).isEqualTo("USD");
        assertThat(entity.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(entity.getVersion()).isZero();
    }

    @Test
    void toDomainReconstitutesNonZeroVersion() {
        AccountJpaEntity entity = new AccountJpaEntity();
        entity.setAccountId(java.util.UUID.randomUUID());
        entity.setCurrency("USD");
        entity.setBalance(new BigDecimal("42.50"));
        entity.setVersion(7L);

        Account account = AccountMapper.toDomain(entity);

        assertThat(account.getBalance()).isEqualTo(Money.of(new BigDecimal("42.50"), USD));
        assertThat(account.getVersion()).isEqualTo(7L);
    }
}
