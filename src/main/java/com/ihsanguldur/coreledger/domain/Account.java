package com.ihsanguldur.coreledger.domain;

import com.ihsanguldur.coreledger.domain.event.DomainEvent;
import com.ihsanguldur.coreledger.domain.event.MoneyCredited;
import com.ihsanguldur.coreledger.domain.event.MoneyDebited;
import com.ihsanguldur.coreledger.domain.exception.InsufficientFundsException;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import com.ihsanguldur.coreledger.domain.valueobject.Money;
import com.ihsanguldur.coreledger.domain.valueobject.TransactionId;
import lombok.Getter;

import java.time.Instant;
import java.util.*;

public final class Account {

    @Getter
    private final AccountId accountId;

    @Getter
    private Money balance;

    @Getter
    private long version;

    private final Set<TransactionId> appliedTransactions = new HashSet<>();

    private final List<DomainEvent> events = new ArrayList<>();

    public List<DomainEvent> getEvents() {
        return List.copyOf(events);
    }

    private Account(AccountId accountId, Currency currency) {
        this.accountId = Objects.requireNonNull(accountId, "accountId cannot be null");
        Objects.requireNonNull(currency, "currency cannot be null");
        this.balance = Money.zero(currency);
        this.version = 0L;
    }

    public static Account open(AccountId accountId, Currency currency) {
        return new Account(accountId, currency);
    }

    public void debit(Money amount, TransactionId transactionId) {
        Objects.requireNonNull(amount, "amount cannot be null");
        Objects.requireNonNull(transactionId, "transactionId cannot be null");

        if (appliedTransactions.contains(transactionId)) {
            return;
        }

        if (!balance.isGreaterThanOrEqualTo(amount)) {
            throw new InsufficientFundsException(accountId, balance, amount);
        }

        this.balance = balance.subtract(amount);
        appliedTransactions.add(transactionId);
        events.add(new MoneyDebited(accountId, amount, transactionId, Instant.now()));
    }

    public void credit(Money amount, TransactionId transactionId) {
        Objects.requireNonNull(amount, "amount cannot be null");
        Objects.requireNonNull(transactionId, "transactionId cannot be null");

        if (appliedTransactions.contains(transactionId)) {
            return;
        }

        this.balance = balance.add(amount);
        appliedTransactions.add(transactionId);
        events.add(new MoneyCredited(accountId, amount, transactionId, Instant.now()));
    }
}
