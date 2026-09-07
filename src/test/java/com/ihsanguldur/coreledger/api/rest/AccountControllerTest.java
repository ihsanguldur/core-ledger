package com.ihsanguldur.coreledger.api.rest;

import com.ihsanguldur.coreledger.application.GetAccountBalanceUseCase;
import com.ihsanguldur.coreledger.application.GetAccountHistoryUseCase;
import com.ihsanguldur.coreledger.application.OpenAccountUseCase;
import com.ihsanguldur.coreledger.config.WebConfig;
import com.ihsanguldur.coreledger.domain.Account;
import com.ihsanguldur.coreledger.domain.entity.LedgerEntry;
import com.ihsanguldur.coreledger.domain.exception.AccountNotFoundException;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import com.ihsanguldur.coreledger.domain.valueobject.EntryId;
import com.ihsanguldur.coreledger.domain.valueobject.Money;
import com.ihsanguldur.coreledger.domain.valueobject.TransactionId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import(WebConfig.class)
class AccountControllerTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OpenAccountUseCase openAccountUseCase;

    @MockBean
    private GetAccountBalanceUseCase getAccountBalanceUseCase;

    @MockBean
    private GetAccountHistoryUseCase getAccountHistoryUseCase;

    @Test
    void openAccountReturns201WithAccountDetails() throws Exception {
        AccountId accountId = AccountId.generate();
        Account account = Account.open(accountId, USD);
        when(openAccountUseCase.open(eq(USD))).thenReturn(account);

        mockMvc.perform(post("/api/accounts")
                        .contentType("application/json")
                        .content("{\"currency\":\"USD\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(accountId.value().toString()))
                .andExpect(jsonPath("$.balance").value(0.0))
                .andExpect(jsonPath("$.currency").value("USD"));

        verify(openAccountUseCase).open(USD);
    }

    @Test
    void getBalanceReturns200WithBalanceDetails() throws Exception {
        AccountId accountId = AccountId.generate();
        Account account = Account.open(accountId, USD);
        account.credit(Money.of(new BigDecimal("100.00"), USD), TransactionId.generate());
        when(getAccountBalanceUseCase.getBalance(eq(accountId))).thenReturn(account);

        mockMvc.perform(get("/api/accounts/{id}/balance", accountId.value()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId.value().toString()))
                .andExpect(jsonPath("$.balance").value(100.0))
                .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    void getBalanceReturns404WhenAccountNotFound() throws Exception {
        AccountId accountId = AccountId.generate();
        when(getAccountBalanceUseCase.getBalance(eq(accountId)))
                .thenThrow(new AccountNotFoundException(accountId));

        mockMvc.perform(get("/api/accounts/{id}/balance", accountId.value()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getHistoryReturns200WithLedgerEntries() throws Exception {
        AccountId accountId = AccountId.generate();
        TransactionId transactionId = TransactionId.generate();
        LedgerEntry entry = new LedgerEntry(
                EntryId.generate(), accountId, LedgerEntry.Direction.CREDIT,
                Money.of(new BigDecimal("50.00"), USD), transactionId, Instant.now()
        );
        when(getAccountHistoryUseCase.getHistory(eq(accountId))).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/accounts/{id}/history", accountId.value()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].entryId").value(entry.getEntryId().value().toString()))
                .andExpect(jsonPath("$[0].direction").value("CREDIT"))
                .andExpect(jsonPath("$[0].amount").value(50.0))
                .andExpect(jsonPath("$[0].currency").value("USD"))
                .andExpect(jsonPath("$[0].transactionId").value(transactionId.value().toString()));
    }

    @Test
    void getHistoryReturns404WhenAccountNotFound() throws Exception {
        AccountId accountId = AccountId.generate();
        when(getAccountHistoryUseCase.getHistory(eq(accountId)))
                .thenThrow(new AccountNotFoundException(accountId));

        mockMvc.perform(get("/api/accounts/{id}/history", accountId.value()))
                .andExpect(status().isNotFound());
    }
}