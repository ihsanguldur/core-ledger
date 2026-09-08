package com.ihsanguldur.coreledger.api.rest;

import com.ihsanguldur.coreledger.api.dto.request.DepositRequest;
import com.ihsanguldur.coreledger.api.dto.request.OpenAccountRequest;
import com.ihsanguldur.coreledger.api.dto.response.BalanceResponse;
import com.ihsanguldur.coreledger.api.dto.response.LedgerEntryResponse;
import com.ihsanguldur.coreledger.api.dto.response.OpenAccountResponse;
import com.ihsanguldur.coreledger.application.DepositMoneyUseCase;
import com.ihsanguldur.coreledger.application.GetAccountBalanceUseCase;
import com.ihsanguldur.coreledger.application.GetAccountHistoryUseCase;
import com.ihsanguldur.coreledger.application.OpenAccountUseCase;
import com.ihsanguldur.coreledger.domain.Account;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import com.ihsanguldur.coreledger.domain.valueobject.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Currency;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final OpenAccountUseCase openAccountUseCase;
    private final GetAccountBalanceUseCase getAccountBalanceUseCase;
    private final GetAccountHistoryUseCase getAccountHistoryUseCase;
    private final DepositMoneyUseCase depositMoneyUseCase;

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

    @GetMapping("/{id}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable UUID id) {
        Account account = getAccountBalanceUseCase.getBalance(AccountId.of(id));

        BalanceResponse response = new BalanceResponse(
                account.getAccountId().value(),
                account.getBalance().getAmount(),
                account.getBalance().getCurrency().getCurrencyCode()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<LedgerEntryResponse>> getHistory(@PathVariable UUID id) {
        List<LedgerEntryResponse> response = getAccountHistoryUseCase.getHistory(AccountId.of(id)).stream()
                .map(entry -> new LedgerEntryResponse(
                        entry.getEntryId().value(),
                        entry.getDirection().name(),
                        entry.getAmount().getAmount(),
                        entry.getAmount().getCurrency().getCurrencyCode(),
                        entry.getTransactionId().value(),
                        entry.getCreatedAt()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<BalanceResponse> deposit(@PathVariable UUID id, @RequestBody DepositRequest request) {
        Account account = depositMoneyUseCase.deposit(
                AccountId.of(id), Money.of(request.amount(), Currency.getInstance(request.currency()))
        );

        BalanceResponse response = new BalanceResponse(
                account.getAccountId().value(),
                account.getBalance().getAmount(),
                account.getBalance().getCurrency().getCurrencyCode()
        );

        return ResponseEntity.ok(response);
    }
}
