package com.ihsanguldur.coreledger.domain.exception;

import java.util.Currency;

public class CurrencyMismatchException extends RuntimeException {
    public CurrencyMismatchException(Currency expected, Currency actual) {
        super("currency mismatch: expected" + expected.getCurrencyCode() + " but got " + actual.getCurrencyCode());
    }
}