package com.ihsanguldur.coreledger.application.external;

import com.ihsanguldur.coreledger.application.external.exception.ExternalBankGatewayException;
import com.ihsanguldur.coreledger.application.external.port.ExternalBankGatewayPort;
import com.ihsanguldur.coreledger.application.port.AccountRepository;
import com.ihsanguldur.coreledger.domain.Account;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import com.ihsanguldur.coreledger.domain.valueobject.Money;
import com.ihsanguldur.coreledger.domain.valueobject.TransactionId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransferToExternalBankUseCaseTest {

    private static final Currency USD = Currency.getInstance("USD");
    private static final String DESTINATION_REF = "EXT-REF-123";

    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final ExternalBankGatewayPort externalBankGatewayPort = mock(ExternalBankGatewayPort.class);
    private final TransferToExternalBankUseCase useCase =
            new TransferToExternalBankUseCase(accountRepository, externalBankGatewayPort);

    @Test
    void acceptedPaymentDebitsOnceAndDoesNotCompensate() {
        AccountId accountId = AccountId.generate();
        Account account = fundedAccount(accountId, "100.00");
        Money amount = Money.of(new BigDecimal("30.00"), USD);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(externalBankGatewayPort.submitPayment(DESTINATION_REF, amount)).thenReturn(true);

        useCase.transfer(accountId, DESTINATION_REF, amount);

        assertThat(account.getBalance()).isEqualTo(Money.of(new BigDecimal("70.00"), USD));
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void rejectedPaymentCompensatesByCreditingBackTheDebitedAmount() {
        AccountId accountId = AccountId.generate();
        Account account = fundedAccount(accountId, "100.00");
        Money amount = Money.of(new BigDecimal("30.00"), USD);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(externalBankGatewayPort.submitPayment(DESTINATION_REF, amount)).thenReturn(false);

        useCase.transfer(accountId, DESTINATION_REF, amount);

        assertThat(account.getBalance()).isEqualTo(Money.of(new BigDecimal("100.00"), USD));
        verify(accountRepository, times(2)).save(account); // 1x debit + 1x compensating credit
    }

    @Test
    void gatewayFailureCompensatesAndRethrowsTheOriginalException() {
        AccountId accountId = AccountId.generate();
        Account account = fundedAccount(accountId, "100.00");
        Money amount = Money.of(new BigDecimal("30.00"), USD);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        ExternalBankGatewayException gatewayException =
                new ExternalBankGatewayException("boom", new RuntimeException("connection refused"));
        when(externalBankGatewayPort.submitPayment(DESTINATION_REF, amount)).thenThrow(gatewayException);

        assertThatThrownBy(() -> useCase.transfer(accountId, DESTINATION_REF, amount))
                .isSameAs(gatewayException);

        assertThat(account.getBalance()).isEqualTo(Money.of(new BigDecimal("100.00"), USD));
        verify(accountRepository, times(2)).save(account); // 1x debit + 1x compensating credit
    }

    private Account fundedAccount(AccountId accountId, String initialBalance) {
        Account account = Account.open(accountId, USD);
        account.credit(Money.of(new BigDecimal(initialBalance), USD), TransactionId.generate());
        return account;
    }
}