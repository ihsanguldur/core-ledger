package com.ihsanguldur.coreledger.infrastructure.persistence.mapper;

import com.ihsanguldur.coreledger.domain.entity.LedgerEntry;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import com.ihsanguldur.coreledger.domain.valueobject.EntryId;
import com.ihsanguldur.coreledger.domain.valueobject.Money;
import com.ihsanguldur.coreledger.domain.valueobject.TransactionId;
import com.ihsanguldur.coreledger.infrastructure.persistence.jpaentity.LedgerEntryJpaEntity;

import java.util.Currency;

public final class LedgerEntryMapper {

    private LedgerEntryMapper() {
    }

    public static LedgerEntryJpaEntity toJpaEntity(LedgerEntry ledgerEntry) {
        LedgerEntryJpaEntity entity = new LedgerEntryJpaEntity();
        entity.setEntryId(ledgerEntry.getEntryId().value());
        entity.setAccountId(ledgerEntry.getAccountId().value());
        entity.setDirection(ledgerEntry.getDirection());
        entity.setAmount(ledgerEntry.getAmount().getAmount());
        entity.setCurrency(ledgerEntry.getAmount().getCurrency().getCurrencyCode());
        entity.setTransactionId(ledgerEntry.getTransactionId().value());
        entity.setCreatedAt(ledgerEntry.getCreatedAt());
        return entity;
    }

    public static LedgerEntry toDomain(LedgerEntryJpaEntity entity) {
        Money amount = Money.of(entity.getAmount(), Currency.getInstance(entity.getCurrency()));
        return new LedgerEntry(
                EntryId.of(entity.getEntryId()),
                AccountId.of(entity.getAccountId()),
                entity.getDirection(),
                amount,
                TransactionId.of(entity.getTransactionId()),
                entity.getCreatedAt()
        );
    }
}
