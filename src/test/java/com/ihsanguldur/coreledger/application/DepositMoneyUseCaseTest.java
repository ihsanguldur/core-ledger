package com.ihsanguldur.coreledger.application;

import com.ihsanguldur.coreledger.application.port.AccountRepository;
import com.ihsanguldur.coreledger.domain.Account;
import com.ihsanguldur.coreledger.domain.exception.AccountNotFoundException;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import com.ihsanguldur.coreledger.domain.valueobject.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DepositMoneyUseCaseTest {

    private static final Currency USD = Currency.getInstance("USD");

    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final DepositMoneyUseCase useCase = new DepositMoneyUseCase(accountRepository);

    @Test
    void creditsAccountAndPersistsIt() {
        AccountId accountId = AccountId.generate();
        Account account = Account.open(accountId, USD);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        Account result = useCase.deposit(accountId, Money.of(new BigDecimal("100.00"), USD));

        assertThat(result.getBalance()).isEqualTo(Money.of(new BigDecimal("100.00"), USD));
        verify(accountRepository).save(account);
    }

    @Test
    void throwsAccountNotFoundWhenMissing() {
        AccountId accountId = AccountId.generate();
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.deposit(accountId, Money.of(new BigDecimal("100.00"), USD)))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
