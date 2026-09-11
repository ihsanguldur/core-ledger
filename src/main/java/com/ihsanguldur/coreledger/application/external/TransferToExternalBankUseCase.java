package com.ihsanguldur.coreledger.application.external;

import com.ihsanguldur.coreledger.application.external.port.ExternalBankGatewayPort;
import com.ihsanguldur.coreledger.application.port.AccountRepository;
import com.ihsanguldur.coreledger.domain.Account;
import com.ihsanguldur.coreledger.domain.exception.AccountNotFoundException;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import com.ihsanguldur.coreledger.domain.valueobject.Money;
import com.ihsanguldur.coreledger.domain.valueobject.TransactionId;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransferToExternalBankUseCase {

    private final AccountRepository accountRepository;
    private final ExternalBankGatewayPort externalBankGatewayPort;
    private final MeterRegistry meterRegistry;

    public boolean transfer(AccountId sourceId, String destinationAccountRef, Money amount) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            debit(sourceId, amount);

            boolean accepted;
            try {
                accepted = externalBankGatewayPort.submitPayment(destinationAccountRef, amount);
            } catch (RuntimeException e) {
                log.error("external bank gateway call failed for account={}, compensating", sourceId, e);
                credit(sourceId, amount);
                meterRegistry.counter("ledger.external_transfer.attempts", "outcome", "error").increment();
                throw e;
            }

            if (!accepted) {
                log.warn("external bank rejected payment for account={}, compensating", sourceId);
                credit(sourceId, amount);
            }
            meterRegistry.counter(
                    "ledger.external_transfer.attempts", "outcome", accepted ? "accepted" : "rejected"
            ).increment();
            return accepted;
        } finally {
            sample.stop(meterRegistry.timer("ledger.external_transfer.duration"));
        }
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