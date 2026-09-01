package com.ihsanguldur.coreledger.domain.entity;

import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import com.ihsanguldur.coreledger.domain.valueobject.EntryId;
import com.ihsanguldur.coreledger.domain.valueobject.Money;
import com.ihsanguldur.coreledger.domain.valueobject.TransactionId;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

@Getter
@EqualsAndHashCode(of = "entryId")
public final class LedgerEntry {

    private final EntryId entryId;
    private final AccountId accountId;
    private final Direction direction;
    private final Money amount;
    private final TransactionId transactionId;
    private final Instant createdAt;

    public LedgerEntry(EntryId entryId, AccountId accountId, Direction direction,
                       Money amount, TransactionId transactionId, Instant createdAt) {
        this.entryId = Objects.requireNonNull(entryId, "entryId cannot be null");
        this.accountId = Objects.requireNonNull(accountId, "accountId cannot be null");
        this.direction = Objects.requireNonNull(direction, "direction cannot be null");
        this.amount = Objects.requireNonNull(amount, "amount cannot be null");
        this.transactionId = Objects.requireNonNull(transactionId, "transactionId cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt cannot be null");
    }

    public enum Direction {
        DEBIT, CREDIT
    }

}
