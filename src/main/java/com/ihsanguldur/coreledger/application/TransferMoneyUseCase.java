package com.ihsanguldur.coreledger.application;

import com.ihsanguldur.coreledger.application.port.AccountRepository;
import com.ihsanguldur.coreledger.application.port.IdempotencyKeyRepository;
import com.ihsanguldur.coreledger.domain.Account;
import com.ihsanguldur.coreledger.domain.exception.AccountNotFoundException;
import com.ihsanguldur.coreledger.domain.service.TransferService;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import com.ihsanguldur.coreledger.domain.valueobject.IdempotencyKey;
import com.ihsanguldur.coreledger.domain.valueobject.Money;
import com.ihsanguldur.coreledger.domain.valueobject.TransactionId;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.transaction.Transactional;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
public class TransferMoneyUseCase {

    private final AccountRepository accountRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final MeterRegistry meterRegistry;

    public TransferMoneyUseCase(
            AccountRepository accountRepository,
            IdempotencyKeyRepository idempotencyKeyRepository,
            MeterRegistry meterRegistry
    ) {
        this.accountRepository = accountRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.meterRegistry = meterRegistry;
    }

    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 50, multiplier = 2)
    )
    @Transactional
    public void transfer(AccountId sourceId, AccountId destinationId, Money amount, IdempotencyKey idempotencyKey) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            if (idempotencyKeyRepository.exists(idempotencyKey)) {
                return;
            }

            Account source = accountRepository.findById(sourceId)
                    .orElseThrow(() -> new AccountNotFoundException(sourceId));
            Account destination = accountRepository.findById(destinationId)
                    .orElseThrow(() -> new AccountNotFoundException(destinationId));

            TransferService.transfer(source, destination, amount, TransactionId.generate());

            accountRepository.save(source);
            accountRepository.save(destination);
            idempotencyKeyRepository.save(idempotencyKey);

            meterRegistry.counter("ledger.transfer.attempts", "outcome", "success").increment();
        } catch (RuntimeException e) {
            meterRegistry.counter("ledger.transfer.attempts", "outcome", "failure").increment();
            throw e;
        } finally {
            sample.stop(meterRegistry.timer("ledger.transfer.duration"));
        }
    }
}