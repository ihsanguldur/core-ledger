package com.ihsanguldur.coreledger.application;

import com.ihsanguldur.coreledger.application.port.AccountRepository;
import com.ihsanguldur.coreledger.domain.Account;
import com.ihsanguldur.coreledger.domain.valueobject.Money;
import org.junit.jupiter.api.Test;

import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OpenAccountUseCaseTest {

    private static final Currency USD = Currency.getInstance("USD");

    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final OpenAccountUseCase useCase = new OpenAccountUseCase(accountRepository);

    @Test
    void opensAccountWithZeroBalanceAndPersistsIt() {
        Account account = useCase.open(USD);

        assertThat(account.getBalance()).isEqualTo(Money.zero(USD));
        verify(accountRepository).save(account);
    }

    @Test
    void eachCallOpensADifferentAccount() {
        Account first = useCase.open(USD);
        Account second = useCase.open(USD);

        assertThat(first.getAccountId()).isNotEqualTo(second.getAccountId());
    }
}
