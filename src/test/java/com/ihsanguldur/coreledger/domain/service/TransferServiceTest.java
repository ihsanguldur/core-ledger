package com.ihsanguldur.coreledger.domain.service;

import com.ihsanguldur.coreledger.domain.Account;
import com.ihsanguldur.coreledger.domain.exception.InsufficientFundsException;
import com.ihsanguldur.coreledger.domain.exception.InvalidAmountException;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import com.ihsanguldur.coreledger.domain.valueobject.Money;
import com.ihsanguldur.coreledger.domain.valueobject.TransactionId;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransferServiceTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void happyPathTransfersMoneyBetweenAccounts() {
        Account source = Account.open(AccountId.generate(), USD);
        Account destination = Account.open(AccountId.generate(), USD);
        source.credit(Money.of(new BigDecimal("100.00"), USD), TransactionId.generate());

        TransferService.transfer(source, destination, Money.of(new BigDecimal("40.00"), USD), TransactionId.generate());

        assertThat(source.getBalance()).isEqualTo(Money.of(new BigDecimal("60.00"), USD));
        assertThat(destination.getBalance()).isEqualTo(Money.of(new BigDecimal("40.00"), USD));
    }

    @Test
    void insufficientFundsOnSourcePreventsTransfer() {
        Account source = Account.open(AccountId.generate(), USD);
        Account destination = Account.open(AccountId.generate(), USD);

        assertThatThrownBy(() -> TransferService.transfer(
                source, destination, Money.of(new BigDecimal("10.00"), USD), TransactionId.generate()))
                .isInstanceOf(InsufficientFundsException.class);

        assertThat(destination.getBalance()).isEqualTo(Money.zero(USD));
    }

    @Test
    void compensatesSourceWhenDestinationCreditFails() {
        Account source = Account.open(AccountId.generate(), USD);
        source.credit(Money.of(new BigDecimal("100.00"), USD), TransactionId.generate());
        Money balanceBeforeTransfer = source.getBalance();

        Account destination = Mockito.mock(Account.class);
        Mockito.doThrow(new RuntimeException("simulated failure"))
                .when(destination).credit(Mockito.any(), Mockito.any());

        assertThatThrownBy(() -> TransferService.transfer(
                source, destination, Money.of(new BigDecimal("30.00"), USD), TransactionId.generate()))
                .isInstanceOf(RuntimeException.class);

        assertThat(source.getBalance()).isEqualTo(balanceBeforeTransfer);
    }

    @Test
    void zeroAmountTransferIsRejected() {
        Account source = Account.open(AccountId.generate(), USD);
        source.credit(Money.of(new BigDecimal("100.00"), USD), TransactionId.generate());
        Account destination = Account.open(AccountId.generate(), USD);

        assertThatThrownBy(() -> TransferService.transfer(
                source, destination, Money.zero(USD), TransactionId.generate()))
                .isInstanceOf(InvalidAmountException.class);

        assertThat(destination.getBalance()).isEqualTo(Money.zero(USD));
    }
}
