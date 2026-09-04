package com.ihsanguldur.coreledger.domain.exception;

import com.ihsanguldur.coreledger.domain.valueobject.Money;

public class InvalidAmountException extends RuntimeException {
    public InvalidAmountException(Money amount) {
        super("amount must be greater than zero: " + amount);
    }
}
