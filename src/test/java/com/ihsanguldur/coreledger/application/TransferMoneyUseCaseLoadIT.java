package com.ihsanguldur.coreledger.application;

import com.ihsanguldur.coreledger.application.port.AccountRepository;
import com.ihsanguldur.coreledger.domain.Account;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import com.ihsanguldur.coreledger.domain.valueobject.IdempotencyKey;
import com.ihsanguldur.coreledger.domain.valueobject.Money;
import com.ihsanguldur.coreledger.domain.valueobject.TransactionId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Faz 17 verify: 100 eşzamanlı transferden sonra bakiye matematiksel olarak doğru olmalı, para
 * kaybolmamalı/çoğalmamalı. Bu ölçekte (aynı hesaba 100-way çarpışma) maxAttempts=3'lük retry
 * politikası bazı isteklerin retry limitini gerçekten aşmasına yol açabilir — bu beklenen bir
 * durum. Test "hepsi başarılı olsun" demiyor, "kaç tanesi başarılı olduysa bakiye ona göre doğru
 * mu" diyor: her başarılı transfer doğru yansımalı, başarısız olan hiçbir iz bırakmamalı (ne
 * kaynaktan düşmeli ne hedefe eklenmeli).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
class TransferMoneyUseCaseLoadIT {

    private static final Currency USD = Currency.getInstance("USD");
    private static final int CONCURRENT_TRANSFERS = 100;
    private static final BigDecimal TRANSFER_AMOUNT = new BigDecimal("1.00");
    private static final BigDecimal INITIAL_FUNDING = new BigDecimal("1000.00");

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private OpenAccountUseCase openAccountUseCase;

    @Autowired
    private TransferMoneyUseCase transferMoneyUseCase;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void hundredConcurrentTransfersOnSameAccountKeepBalanceMathematicallyConsistent() throws Exception {
        AccountId sourceId = openAccountUseCase.open(USD).getAccountId();
        AccountId destinationId = openAccountUseCase.open(USD).getAccountId();
        fund(sourceId, INITIAL_FUNDING);

        int successCount = runConcurrentTransfersAndCountSuccesses(sourceId, destinationId, CONCURRENT_TRANSFERS);

        assertThat(successCount).isGreaterThan(0);

        BigDecimal expectedTransferred = TRANSFER_AMOUNT.multiply(BigDecimal.valueOf(successCount));
        Account source = accountRepository.findById(sourceId).orElseThrow();
        Account destination = accountRepository.findById(destinationId).orElseThrow();

        assertThat(source.getBalance().getAmount()).isEqualByComparingTo(INITIAL_FUNDING.subtract(expectedTransferred));
        assertThat(destination.getBalance().getAmount()).isEqualByComparingTo(expectedTransferred);
    }

    private void fund(AccountId accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId).orElseThrow();
        account.credit(Money.of(amount, USD), TransactionId.generate());
        accountRepository.save(account);
    }

    private int runConcurrentTransfersAndCountSuccesses(AccountId sourceId, AccountId destinationId, int count)
            throws InterruptedException {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        List<Future<Void>> futures = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            futures.add(executor.submit(() -> {
                transferMoneyUseCase.transfer(
                        sourceId, destinationId, Money.of(TRANSFER_AMOUNT, USD), IdempotencyKey.of(UUID.randomUUID())
                );
                return null;
            }));
        }
        executor.shutdown();

        int successCount = 0;
        for (Future<Void> future : futures) {
            try {
                future.get(60, TimeUnit.SECONDS);
                successCount++;
            } catch (ExecutionException e) {
                // retry limiti aşıldı (ObjectOptimisticLockingFailureException) — 100-way çarpışmada
                // beklenen bir durum, bu isteği başarısız say ama testi patlatma.
            } catch (TimeoutException e) {
                throw new AssertionError("transfer did not complete within timeout", e);
            }
        }
        return successCount;
    }
}
