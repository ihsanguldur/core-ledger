package com.ihsanguldur.coreledger.application;

import com.ihsanguldur.coreledger.application.port.AccountRepository;
import com.ihsanguldur.coreledger.domain.Account;
import com.ihsanguldur.coreledger.domain.exception.AccountNotFoundException;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import org.junit.jupiter.api.Test;

import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetAccountBalanceUseCaseTest {

    private static final Currency USD = Currency.getInstance("USD");

    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final GetAccountBalanceUseCase useCase = new GetAccountBalanceUseCase(accountRepository);

    @Test
    void returnsAccountWhenFound() {
        AccountId accountId = AccountId.generate();
        Account account = Account.open(accountId, USD);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        Account result = useCase.getBalance(accountId);

        assertThat(result).isEqualTo(account);
    }

    @Test
    void throwsAccountNotFoundWhenMissing() {
        AccountId accountId = AccountId.generate();
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getBalance(accountId))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
