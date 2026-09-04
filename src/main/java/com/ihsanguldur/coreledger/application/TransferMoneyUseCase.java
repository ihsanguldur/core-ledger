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
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class TransferMoneyUseCase {

    private final AccountRepository accountRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;

    public TransferMoneyUseCase(
            AccountRepository accountRepository, IdempotencyKeyRepository idempotencyKeyRepository
    ) {
        this.accountRepository = accountRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
    }

    @Transactional
    public void transfer(AccountId sourceId, AccountId destinationId, Money amount, IdempotencyKey idempotencyKey) {
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
    }
}
