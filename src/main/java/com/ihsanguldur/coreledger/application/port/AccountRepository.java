package com.ihsanguldur.coreledger.application.port;

import com.ihsanguldur.coreledger.domain.Account;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;

import java.util.Optional;

public interface AccountRepository {

    Optional<Account> findById(AccountId accountId);

    void save(Account account);
}
