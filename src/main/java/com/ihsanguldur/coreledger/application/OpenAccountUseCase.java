package com.ihsanguldur.coreledger.application;

import com.ihsanguldur.coreledger.application.port.AccountRepository;
import com.ihsanguldur.coreledger.domain.Account;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Currency;

@Service
@RequiredArgsConstructor
public class OpenAccountUseCase {

    private final AccountRepository accountRepository;

    public Account open(Currency currency) {
        Account account = Account.open(AccountId.generate(), currency);
        accountRepository.save(account);
        return account;
    }
}
