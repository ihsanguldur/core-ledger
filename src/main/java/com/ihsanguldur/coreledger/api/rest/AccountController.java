package com.ihsanguldur.coreledger.api.rest;

import com.ihsanguldur.coreledger.api.dto.OpenAccountRequest;
import com.ihsanguldur.coreledger.api.dto.OpenAccountResponse;
import com.ihsanguldur.coreledger.application.OpenAccountUseCase;
import com.ihsanguldur.coreledger.domain.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Currency;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final OpenAccountUseCase openAccountUseCase;

    @PostMapping
    public ResponseEntity<OpenAccountResponse> openAccount(@RequestBody OpenAccountRequest request) {
        Account account = openAccountUseCase.open(Currency.getInstance(request.currency()));

        OpenAccountResponse response = new OpenAccountResponse(
                account.getAccountId().value(),
                account.getBalance().getAmount(),
                account.getBalance().getCurrency().getCurrencyCode()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
