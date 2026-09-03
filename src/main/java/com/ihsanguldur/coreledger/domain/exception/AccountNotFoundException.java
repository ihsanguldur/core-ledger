package com.ihsanguldur.coreledger.domain.exception;

import com.ihsanguldur.coreledger.domain.valueobject.AccountId;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(AccountId accountId) {
        super("account not found: " + accountId);
    }
}
