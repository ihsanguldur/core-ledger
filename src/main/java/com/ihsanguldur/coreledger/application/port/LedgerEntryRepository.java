package com.ihsanguldur.coreledger.application.port;

import com.ihsanguldur.coreledger.domain.entity.LedgerEntry;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;

import java.util.List;

public interface LedgerEntryRepository {
    List<LedgerEntry> findByAccountId(AccountId accountId);
}
