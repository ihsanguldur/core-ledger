package com.ihsanguldur.coreledger.application;

import com.ihsanguldur.coreledger.application.port.AccountRepository;
import com.ihsanguldur.coreledger.application.port.IdempotencyKeyRepository;
import com.ihsanguldur.coreledger.domain.Account;
import com.ihsanguldur.coreledger.domain.exception.AccountNotFoundException;
import com.ihsanguldur.coreledger.domain.exception.InsufficientFundsException;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import com.ihsanguldur.coreledger.domain.valueobject.IdempotencyKey;
import com.ihsanguldur.coreledger.domain.valueobject.Money;
import com.ihsanguldur.coreledger.domain.valueobject.TransactionId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransferMoneyUseCaseTest {

    private static final Currency USD = Currency.getInstance("USD");

    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final IdempotencyKeyRepository idempotencyKeyRepository = mock(IdempotencyKeyRepository.class);
    private final TransferMoneyUseCase useCase = new TransferMoneyUseCase(accountRepository, idempotencyKeyRepository);

    @Test
    void happyPathTransfersMoneyAndPersistsBothAccounts() {
        AccountId sourceId = AccountId.generate();
        AccountId destinationId = AccountId.generate();
        Account source = Account.open(sourceId, USD);
        source.credit(Money.of(new BigDecimal("100.00"), USD), TransactionId.generate());
        Account destination = Account.open(destinationId, USD);

        when(idempotencyKeyRepository.exists(any())).thenReturn(false);
        when(accountRepository.findById(sourceId)).thenReturn(Optional.of(source));
        when(accountRepository.findById(destinationId)).thenReturn(Optional.of(destination));

        IdempotencyKey key = IdempotencyKey.of(UUID.randomUUID());
        useCase.transfer(sourceId, destinationId, Money.of(new BigDecimal("40.00"), USD), key);

        assertThat(source.getBalance()).isEqualTo(Money.of(new BigDecimal("60.00"), USD));
        assertThat(destination.getBalance()).isEqualTo(Money.of(new BigDecimal("40.00"), USD));
        verify(accountRepository).save(source);
        verify(accountRepository).save(destination);
        verify(idempotencyKeyRepository).save(key);
    }

    @Test
    void insufficientFundsPropagatesAndDoesNotSave() {
        AccountId sourceId = AccountId.generate();
        AccountId destinationId = AccountId.generate();
        Account source = Account.open(sourceId, USD);
        Account destination = Account.open(destinationId, USD);

        when(idempotencyKeyRepository.exists(any())).thenReturn(false);
        when(accountRepository.findById(sourceId)).thenReturn(Optional.of(source));
        when(accountRepository.findById(destinationId)).thenReturn(Optional.of(destination));

        assertThatThrownBy(() -> useCase.transfer(
                sourceId, destinationId, Money.of(new BigDecimal("10.00"), USD), IdempotencyKey.of(UUID.randomUUID())))
                .isInstanceOf(InsufficientFundsException.class);

        verify(accountRepository, never()).save(any());
        verify(idempotencyKeyRepository, never()).save(any());
    }

    @Test
    void repeatedIdempotencyKeyIsNoOp() {
        IdempotencyKey key = IdempotencyKey.of(UUID.randomUUID());
        when(idempotencyKeyRepository.exists(key)).thenReturn(true);

        useCase.transfer(AccountId.generate(), AccountId.generate(), Money.of(new BigDecimal("10.00"), USD), key);

        verifyNoInteractions(accountRepository);
    }

    @Test
    void sourceAccountNotFoundThrows() {
        AccountId sourceId = AccountId.generate();
        AccountId destinationId = AccountId.generate();

        when(idempotencyKeyRepository.exists(any())).thenReturn(false);
        when(accountRepository.findById(sourceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.transfer(
                sourceId, destinationId, Money.of(new BigDecimal("10.00"), USD), IdempotencyKey.of(UUID.randomUUID())))
                .isInstanceOf(AccountNotFoundException.class);
    }
}