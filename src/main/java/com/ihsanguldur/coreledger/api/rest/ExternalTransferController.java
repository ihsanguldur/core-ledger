package com.ihsanguldur.coreledger.api.rest;

import com.ihsanguldur.coreledger.api.dto.request.ExternalTransferRequest;
import com.ihsanguldur.coreledger.api.dto.response.ExternalTransferResponse;
import com.ihsanguldur.coreledger.application.external.TransferToExternalBankUseCase;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import com.ihsanguldur.coreledger.domain.valueobject.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Currency;

@RestController
@RequestMapping("/transfer/external")
@RequiredArgsConstructor
public class ExternalTransferController {

    private final TransferToExternalBankUseCase transferToExternalBankUseCase;

    @PostMapping
    public ResponseEntity<ExternalTransferResponse> transfer(@RequestBody ExternalTransferRequest request) {
        boolean accepted = transferToExternalBankUseCase.transfer(
                AccountId.of(request.sourceAccountId()),
                request.destinationAccountRef(),
                Money.of(request.amount(), Currency.getInstance(request.currency()))
        );

        return ResponseEntity.ok(new ExternalTransferResponse(accepted ? "ACCEPTED" : "REJECTED"));
    }
}
