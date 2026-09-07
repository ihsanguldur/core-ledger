package com.ihsanguldur.coreledger.api.rest;

import com.ihsanguldur.coreledger.api.dto.request.TransferRequest;
import com.ihsanguldur.coreledger.application.TransferMoneyUseCase;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import com.ihsanguldur.coreledger.domain.valueobject.IdempotencyKey;
import com.ihsanguldur.coreledger.domain.valueobject.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Currency;
import java.util.UUID;

@RestController
@RequestMapping("/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final TransferMoneyUseCase transferMoneyUseCase;

    @PostMapping
    public ResponseEntity<Void> transfer(
            @RequestBody TransferRequest request,
            @RequestHeader("Idempotency-Key") UUID idempotencyKeyHeader
    ) {
        Currency currency = Currency.getInstance(request.currency());

        transferMoneyUseCase.transfer(
                AccountId.of(request.sourceAccountId()),
                AccountId.of(request.destinationAccountId()),
                Money.of(request.amount(), currency),
                IdempotencyKey.of(idempotencyKeyHeader)
        );

        return ResponseEntity.ok().build();
    }
}
