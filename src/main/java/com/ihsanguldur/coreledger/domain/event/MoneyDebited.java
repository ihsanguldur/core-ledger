package com.ihsanguldur.coreledger.domain.event;

import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import com.ihsanguldur.coreledger.domain.valueobject.Money;
import com.ihsanguldur.coreledger.domain.valueobject.TransactionId;

import java.time.Instant;
import java.util.Objects;

public record MoneyDebited(
        AccountId accountId, Money amount, TransactionId transactionId, Instant occurredAt
) implements DomainEvent {

    public MoneyDebited {
        Objects.requireNonNull(accountId, "accountId cannot be null");
        Objects.requireNonNull(amount, "amount cannot be null");
        Objects.requireNonNull(transactionId, "transactionId cannot be null");
        Objects.requireNonNull(occurredAt, "occurredAt cannot be null");
    }
}
