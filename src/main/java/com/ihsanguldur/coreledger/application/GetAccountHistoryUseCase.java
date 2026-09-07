package com.ihsanguldur.coreledger.application;

import com.ihsanguldur.coreledger.application.port.AccountRepository;
import com.ihsanguldur.coreledger.application.port.LedgerEntryRepository;
import com.ihsanguldur.coreledger.domain.entity.LedgerEntry;
import com.ihsanguldur.coreledger.domain.exception.AccountNotFoundException;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAccountHistoryUseCase {

    private final AccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public List<LedgerEntry> getHistory(AccountId accountId) {
        if (accountRepository.findById(accountId).isEmpty()) {
            throw new AccountNotFoundException(accountId);
        }

        return ledgerEntryRepository.findByAccountId(accountId);
    }
}
