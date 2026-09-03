package com.ihsanguldur.coreledger.infrastructure.persistence.mapper;

import com.ihsanguldur.coreledger.domain.entity.LedgerEntry;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import com.ihsanguldur.coreledger.domain.valueobject.EntryId;
import com.ihsanguldur.coreledger.domain.valueobject.Money;
import com.ihsanguldur.coreledger.domain.valueobject.TransactionId;
import com.ihsanguldur.coreledger.infrastructure.persistence.jpaentity.LedgerEntryJpaEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

class LedgerEntryMapperTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void roundTripPreservesAllFields() {
        LedgerEntry original = new LedgerEntry(
                EntryId.generate(),
                AccountId.generate(),
                LedgerEntry.Direction.CREDIT,
                Money.of(new BigDecimal("75.25"), USD),
                TransactionId.generate(),
                Instant.now()
        );

        LedgerEntryJpaEntity entity = LedgerEntryMapper.toJpaEntity(original);
        LedgerEntry reconstructed = LedgerEntryMapper.toDomain(entity);

        assertThat(reconstructed.getEntryId()).isEqualTo(original.getEntryId());
        assertThat(reconstructed.getAccountId()).isEqualTo(original.getAccountId());
        assertThat(reconstructed.getDirection()).isEqualTo(original.getDirection());
        assertThat(reconstructed.getAmount()).isEqualTo(original.getAmount());
        assertThat(reconstructed.getTransactionId()).isEqualTo(original.getTransactionId());
        assertThat(reconstructed.getCreatedAt()).isEqualTo(original.getCreatedAt());
    }

    @Test
    void toJpaEntityMapsCurrencyAndDirectionCorrectly() {
        LedgerEntry entry = new LedgerEntry(
                EntryId.generate(),
                AccountId.generate(),
                LedgerEntry.Direction.DEBIT,
                Money.of(new BigDecimal("10.00"), USD),
                TransactionId.generate(),
                Instant.now()
        );

        LedgerEntryJpaEntity entity = LedgerEntryMapper.toJpaEntity(entry);

        assertThat(entity.getCurrency()).isEqualTo("USD");
        assertThat(entity.getDirection()).isEqualTo(LedgerEntry.Direction.DEBIT);
        assertThat(entity.getAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
    }
}