package com.ihsanguldur.coreledger.application.external;

import com.ihsanguldur.coreledger.application.external.exception.ExternalBankGatewayException;
import com.ihsanguldur.coreledger.application.external.port.ExternalBankGatewayPort;
import com.ihsanguldur.coreledger.application.port.AccountRepository;
import com.ihsanguldur.coreledger.domain.Account;
import com.ihsanguldur.coreledger.domain.exception.AccountNotFoundException;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import com.ihsanguldur.coreledger.domain.valueobject.Money;
import com.ihsanguldur.coreledger.domain.valueobject.TransactionId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransferToExternalBankUseCase {

    private final AccountRepository accountRepository;
    private final ExternalBankGatewayPort externalBankGatewayPort;

    public boolean transfer(AccountId sourceId, String destinationAccountRef, Money amount) {
        debit(sourceId, amount);

        boolean accepted;
        try {
            accepted = externalBankGatewayPort.submitPayment(destinationAccountRef, amount);
        } catch (RuntimeException e) {
            log.error("external bank gateway call failed for account={}, compensating", sourceId, e);
            credit(sourceId, amount);
            throw e;
        }

        if (!accepted) {
            log.warn("external bank rejected payment for account={}, compensating", sourceId);
            credit(sourceId, amount);
        }
        return accepted;
    }

    private void debit(AccountId accountId, Money amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        account.debit(amount, TransactionId.generate());
        accountRepository.save(account);
    }

    private void credit(AccountId accountId, Money amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        account.credit(amount, TransactionId.generate());
        accountRepository.save(account);
    }
}
