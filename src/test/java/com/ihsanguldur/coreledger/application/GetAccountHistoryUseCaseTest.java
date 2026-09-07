package com.ihsanguldur.coreledger.application;

import com.ihsanguldur.coreledger.application.port.AccountRepository;
import com.ihsanguldur.coreledger.application.port.LedgerEntryRepository;
import com.ihsanguldur.coreledger.domain.Account;
import com.ihsanguldur.coreledger.domain.entity.LedgerEntry;
import com.ihsanguldur.coreledger.domain.exception.AccountNotFoundException;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import com.ihsanguldur.coreledger.domain.valueobject.EntryId;
import com.ihsanguldur.coreledger.domain.valueobject.Money;
import com.ihsanguldur.coreledger.domain.valueobject.TransactionId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class GetAccountHistoryUseCaseTest {

    private static final Currency USD = Currency.getInstance("USD");

    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final LedgerEntryRepository ledgerEntryRepository = mock(LedgerEntryRepository.class);
    private final GetAccountHistoryUseCase useCase =
            new GetAccountHistoryUseCase(accountRepository, ledgerEntryRepository);

    @Test
    void returnsLedgerEntriesWhenAccountExists() {
        AccountId accountId = AccountId.generate();
        Account account = Account.open(accountId, USD);
        LedgerEntry entry = new LedgerEntry(
                EntryId.generate(), accountId, LedgerEntry.Direction.CREDIT,
                Money.of(new BigDecimal("50.00"), USD), TransactionId.generate(), Instant.now()
        );
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(ledgerEntryRepository.findByAccountId(accountId)).thenReturn(List.of(entry));

        List<LedgerEntry> result = useCase.getHistory(accountId);

        assertThat(result).containsExactly(entry);
    }

    @Test
    void throwsAccountNotFoundAndNeverQueriesLedgerWhenAccountMissing() {
        AccountId accountId = AccountId.generate();
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getHistory(accountId))
                .isInstanceOf(AccountNotFoundException.class);

        verify(ledgerEntryRepository, never()).findByAccountId(accountId);
    }
}