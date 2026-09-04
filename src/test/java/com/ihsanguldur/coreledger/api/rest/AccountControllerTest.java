package com.ihsanguldur.coreledger.api.rest;

import com.ihsanguldur.coreledger.application.OpenAccountUseCase;
import com.ihsanguldur.coreledger.domain.Account;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Currency;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OpenAccountUseCase openAccountUseCase;

    @Test
    void openAccountReturns201WithAccountDetails() throws Exception {
        AccountId accountId = AccountId.generate();
        Account account = Account.open(accountId, USD);
        when(openAccountUseCase.open(eq(USD))).thenReturn(account);

        mockMvc.perform(post("/accounts")
                        .contentType("application/json")
                        .content("{\"currency\":\"USD\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(accountId.value().toString()))
                .andExpect(jsonPath("$.balance").value(0.0))
                .andExpect(jsonPath("$.currency").value("USD"));

        verify(openAccountUseCase).open(USD);
    }
}
