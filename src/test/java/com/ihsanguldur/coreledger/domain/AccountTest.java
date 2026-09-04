package com.ihsanguldur.coreledger.domain;

import com.ihsanguldur.coreledger.domain.event.MoneyCredited;
import com.ihsanguldur.coreledger.domain.event.MoneyDebited;
import com.ihsanguldur.coreledger.domain.exception.InsufficientFundsException;
import com.ihsanguldur.coreledger.domain.exception.InvalidAmountException;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import com.ihsanguldur.coreledger.domain.valueobject.Money;
import com.ihsanguldur.coreledger.domain.valueobject.TransactionId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void debitRejectedWhenInsufficientFunds() {
        Account account = Account.open(AccountId.generate(), USD);

        assertThatThrownBy(() -> account.debit(Money.of(new BigDecimal("10.00"), USD), TransactionId.generate()))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void creditIncreasesBalance() {
        Account account = Account.open(AccountId.generate(), USD);

        account.credit(Money.of(new BigDecimal("50.00"), USD), TransactionId.generate());

        assertThat(account.getBalance()).isEqualTo(Money.of(new BigDecimal("50.00"), USD));
    }

    @Test
    void debitReducesBalance() {
        Account account = Account.open(AccountId.generate(), USD);
        account.credit(Money.of(new BigDecimal("50.00"), USD), TransactionId.generate());

        account.debit(Money.of(new BigDecimal("20.00"), USD), TransactionId.generate());

        assertThat(account.getBalance()).isEqualTo(Money.of(new BigDecimal("30.00"), USD));
    }

    @Test
    void secondDebitWithSameTransactionIdIsNoOp() {
        Account account = Account.open(AccountId.generate(), USD);
        account.credit(Money.of(new BigDecimal("50.00"), USD), TransactionId.generate());
        TransactionId transactionId = TransactionId.generate();

        account.debit(Money.of(new BigDecimal("20.00"), USD), transactionId);
        account.debit(Money.of(new BigDecimal("20.00"), USD), transactionId);

        assertThat(account.getBalance()).isEqualTo(Money.of(new BigDecimal("30.00"), USD));
    }

    @Test
    void secondCreditWithSameTransactionIdIsNoOp() {
        Account account = Account.open(AccountId.generate(), USD);
        TransactionId transactionId = TransactionId.generate();

        account.credit(Money.of(new BigDecimal("20.00"), USD), transactionId);
        account.credit(Money.of(new BigDecimal("20.00"), USD), transactionId);

        assertThat(account.getBalance()).isEqualTo(Money.of(new BigDecimal("20.00"), USD));
    }

    @Test
    void debitProducesMoneyDebitedEvent() {
        Account account = Account.open(AccountId.generate(), USD);
        account.credit(Money.of(new BigDecimal("50.00"), USD), TransactionId.generate());
        TransactionId transactionId = TransactionId.generate();
        Money amount = Money.of(new BigDecimal("20.00"), USD);

        account.debit(amount, transactionId);

        assertThat(account.getEvents())
                .filteredOn(MoneyDebited.class::isInstance)
                .map(MoneyDebited.class::cast)
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.accountId()).isEqualTo(account.getAccountId());
                    assertThat(event.amount()).isEqualTo(amount);
                    assertThat(event.transactionId()).isEqualTo(transactionId);
                });
    }

    @Test
    void creditProducesMoneyCreditedEvent() {
        Account account = Account.open(AccountId.generate(), USD);
        TransactionId transactionId = TransactionId.generate();
        Money amount = Money.of(new BigDecimal("50.00"), USD);

        account.credit(amount, transactionId);

        assertThat(account.getEvents())
                .filteredOn(MoneyCredited.class::isInstance)
                .map(MoneyCredited.class::cast)
                .singleElement()
                .satisfies(event -> {
                    assertThat(event.accountId()).isEqualTo(account.getAccountId());
                    assertThat(event.amount()).isEqualTo(amount);
                    assertThat(event.transactionId()).isEqualTo(transactionId);
                });
    }

    @Test
    void duplicateTransactionIdDoesNotProduceExtraEvent() {
        Account account = Account.open(AccountId.generate(), USD);
        TransactionId transactionId = TransactionId.generate();
        Money amount = Money.of(new BigDecimal("20.00"), USD);

        account.credit(amount, transactionId);
        account.credit(amount, transactionId);

        assertThat(account.getEvents()).hasSize(1);
    }

    @Test
    void debitRejectsZeroAmount() {
        Account account = Account.open(AccountId.generate(), USD);

        assertThatThrownBy(() -> account.debit(Money.zero(USD), TransactionId.generate()))
                .isInstanceOf(InvalidAmountException.class);
    }

    @Test
    void creditRejectsZeroAmount() {
        Account account = Account.open(AccountId.generate(), USD);

        assertThatThrownBy(() -> account.credit(Money.zero(USD), TransactionId.generate()))
                .isInstanceOf(InvalidAmountException.class);
    }

    @Test
    void clearEventsRemovesAllEvents() {
        Account account = Account.open(AccountId.generate(), USD);
        account.credit(Money.of(new BigDecimal("20.00"), USD), TransactionId.generate());

        account.clearEvents();

        assertThat(account.getEvents()).isEmpty();
    }
}