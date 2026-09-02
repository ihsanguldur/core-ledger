package com.ihsanguldur.coreledger.domain.service;

import com.ihsanguldur.coreledger.domain.Account;
import com.ihsanguldur.coreledger.domain.exception.InsufficientFundsException;
import com.ihsanguldur.coreledger.domain.valueobject.Money;
import com.ihsanguldur.coreledger.domain.valueobject.TransactionId;

import java.math.BigDecimal;

public final class TransferService {

    private TransferService() {}

    public static void transfer(Account source, Account destination, Money amount, TransactionId transactionId) {
        source.debit(amount, transactionId);

        try {
            destination.credit(amount, transactionId);
        } catch (RuntimeException e) {
            source.credit(amount, TransactionId.generate());
            throw e;
        }
    }
}
