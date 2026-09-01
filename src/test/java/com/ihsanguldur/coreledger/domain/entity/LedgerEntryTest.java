package com.ihsanguldur.coreledger.domain.entity;

import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import com.ihsanguldur.coreledger.domain.valueobject.EntryId;
import com.ihsanguldur.coreledger.domain.valueobject.Money;
import com.ihsanguldur.coreledger.domain.valueobject.TransactionId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LedgerEntryTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void fieldsAreSetCorrectly() {
        EntryId entryId = EntryId.generate();
        AccountId accountId = AccountId.generate();
        TransactionId transactionId = TransactionId.generate();
        Money amount = Money.of(new BigDecimal("100.00"), USD);
        Instant createdAt = Instant.now();

        LedgerEntry entry = new LedgerEntry(
                entryId, accountId, LedgerEntry.Direction.DEBIT, amount, transactionId, createdAt);

        assertThat(entry.getEntryId()).isEqualTo(entryId);
        assertThat(entry.getAccountId()).isEqualTo(accountId);
        assertThat(entry.getDirection()).isEqualTo(LedgerEntry.Direction.DEBIT);
        assertThat(entry.getAmount()).isEqualTo(amount);
        assertThat(entry.getTransactionId()).isEqualTo(transactionId);
        assertThat(entry.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void entriesWithSameEntryIdAreEqualEvenIfOtherFieldsDiffer() {
        EntryId sharedId = EntryId.generate();
        Money amount = Money.of(new BigDecimal("50.00"), USD);

        LedgerEntry entry1 = new LedgerEntry(
                sharedId, AccountId.generate(), LedgerEntry.Direction.DEBIT, amount, TransactionId.generate(), Instant.now());
        LedgerEntry entry2 = new LedgerEntry(
                sharedId, AccountId.generate(), LedgerEntry.Direction.CREDIT, amount, TransactionId.generate(), Instant.now());

        assertThat(entry1).isEqualTo(entry2);
    }

    @Test
    void entriesWithDifferentEntryIdAreNotEqualEvenIfOtherFieldsMatch() {
        AccountId accountId = AccountId.generate();
        TransactionId transactionId = TransactionId.generate();
        Money amount = Money.of(new BigDecimal("50.00"), USD);
        Instant createdAt = Instant.now();

        LedgerEntry entry1 = new LedgerEntry(
                EntryId.generate(), accountId, LedgerEntry.Direction.DEBIT, amount, transactionId, createdAt);
        LedgerEntry entry2 = new LedgerEntry(
                EntryId.generate(), accountId, LedgerEntry.Direction.DEBIT, amount, transactionId, createdAt);

        assertThat(entry1).isNotEqualTo(entry2);
    }

    @Test
    void rejectsNullEntryId() {
        assertThatThrownBy(() -> new LedgerEntry(
                null, AccountId.generate(), LedgerEntry.Direction.DEBIT,
                Money.zero(USD), TransactionId.generate(), Instant.now()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejectsNullAmount() {
        assertThatThrownBy(() -> new LedgerEntry(
                EntryId.generate(), AccountId.generate(), LedgerEntry.Direction.DEBIT,
                null, TransactionId.generate(), Instant.now()))
                .isInstanceOf(NullPointerException.class);
    }
}