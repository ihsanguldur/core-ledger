package com.ihsanguldur.coreledger.api.rest;

import com.ihsanguldur.coreledger.application.TransferMoneyUseCase;
import com.ihsanguldur.coreledger.config.WebConfig;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import com.ihsanguldur.coreledger.domain.valueobject.IdempotencyKey;
import com.ihsanguldur.coreledger.domain.valueobject.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransferController.class)
@Import(WebConfig.class)
class TransferControllerTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransferMoneyUseCase transferMoneyUseCase;

    @Test
    void transferReturns200AndInvokesUseCaseWithMappedValues() throws Exception {
        UUID sourceAccountId = UUID.randomUUID();
        UUID destinationAccountId = UUID.randomUUID();
        UUID idempotencyKey = UUID.randomUUID();

        mockMvc.perform(post("/api/transfer")
                        .contentType("application/json")
                        .header("Idempotency-Key", idempotencyKey.toString())
                        .content("""
                                {"sourceAccountId":"%s","destinationAccountId":"%s","amount":50.00,"currency":"USD"}
                                """.formatted(sourceAccountId, destinationAccountId)))
                .andExpect(status().isOk());

        verify(transferMoneyUseCase).transfer(
                eq(AccountId.of(sourceAccountId)),
                eq(AccountId.of(destinationAccountId)),
                eq(Money.of(new BigDecimal("50.00"), USD)),
                eq(IdempotencyKey.of(idempotencyKey))
        );
    }

    @Test
    void transferWithoutIdempotencyKeyHeaderReturns400() throws Exception {
        mockMvc.perform(post("/api/transfer")
                        .contentType("application/json")
                        .content("""
                                {"sourceAccountId":"%s","destinationAccountId":"%s","amount":50.00,"currency":"USD"}
                                """.formatted(UUID.randomUUID(), UUID.randomUUID())))
                .andExpect(status().isBadRequest());
    }
}