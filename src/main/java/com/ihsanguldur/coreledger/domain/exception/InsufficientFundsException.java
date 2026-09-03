package com.ihsanguldur.coreledger.domain.exception;

import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import com.ihsanguldur.coreledger.domain.valueobject.Money;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(AccountId accountId, Money balance, Money requested) {
        super("account " + accountId + " has insufficient funds: balance=" + balance + ", requested=" + requested);
    }
}
