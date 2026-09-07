package com.ihsanguldur.coreledger.api.rest;

import com.ihsanguldur.coreledger.application.TransferMoneyUseCase;
import com.ihsanguldur.coreledger.config.WebConfig;
import com.ihsanguldur.coreledger.domain.exception.AccountNotFoundException;
import com.ihsanguldur.coreledger.domain.exception.CurrencyMismatchException;
import com.ihsanguldur.coreledger.domain.exception.InsufficientFundsException;
import com.ihsanguldur.coreledger.domain.exception.InvalidAmountException;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import com.ihsanguldur.coreledger.domain.valueobject.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransferController.class)
@Import(WebConfig.class)
class GlobalExceptionHandlerTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransferMoneyUseCase transferMoneyUseCase;

    @Test
    void accountNotFoundReturns404WithMessage() throws Exception {
        AccountId sourceId = AccountId.generate();
        doThrow(new AccountNotFoundException(sourceId))
                .when(transferMoneyUseCase).transfer(any(), any(), any(), any());

        performTransfer()
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("account not found: " + sourceId));
    }

    @Test
    void insufficientFundsReturns409() throws Exception {
        AccountId sourceId = AccountId.generate();
        Money balance = Money.zero(USD);
        Money requested = Money.of(new BigDecimal("50.00"), USD);
        doThrow(new InsufficientFundsException(sourceId, balance, requested))
                .when(transferMoneyUseCase).transfer(any(), any(), any(), any());

        performTransfer().andExpect(status().isConflict());
    }

    @Test
    void invalidAmountReturns400() throws Exception {
        doThrow(new InvalidAmountException(Money.zero(USD)))
                .when(transferMoneyUseCase).transfer(any(), any(), any(), any());

        performTransfer().andExpect(status().isBadRequest());
    }

    @Test
    void currencyMismatchReturns400() throws Exception {
        doThrow(new CurrencyMismatchException(USD, Currency.getInstance("EUR")))
                .when(transferMoneyUseCase).transfer(any(), any(), any(), any());

        performTransfer().andExpect(status().isBadRequest());
    }

    @Test
    void malformedJsonBodyReturns400WithGenericMessage() throws Exception {
        mockMvc.perform(post("/api/transfer")
                        .contentType("application/json")
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .content("not valid json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("malformed request body"));
    }

    @Test
    void invalidIdempotencyKeyHeaderReturns400() throws Exception {
        mockMvc.perform(post("/api/transfer")
                        .contentType("application/json")
                        .header("Idempotency-Key", "not-a-uuid")
                        .content("""
                                {"sourceAccountId":"%s","destinationAccountId":"%s","amount":50.00,"currency":"USD"}
                                """.formatted(UUID.randomUUID(), UUID.randomUUID())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unexpectedExceptionReturns500WithGenericMessageNotLeakingDetails() throws Exception {
        doThrow(new RuntimeException("db connection refused at 10.0.0.5:5432"))
                .when(transferMoneyUseCase).transfer(any(), any(), any(), any());

        performTransfer()
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("internal server error"));
    }

    private ResultActions performTransfer() throws Exception {
        return mockMvc.perform(post("/api/transfer")
                .contentType("application/json")
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .content("""
                        {"sourceAccountId":"%s","destinationAccountId":"%s","amount":50.00,"currency":"USD"}
                        """.formatted(UUID.randomUUID(), UUID.randomUUID())));
    }
}