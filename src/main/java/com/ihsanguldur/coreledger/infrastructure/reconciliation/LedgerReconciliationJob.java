package com.ihsanguldur.coreledger.infrastructure.reconciliation;

import com.ihsanguldur.coreledger.infrastructure.persistence.jpaentity.AccountJpaEntity;
import com.ihsanguldur.coreledger.infrastructure.persistence.repository.AccountJpaRepository;
import com.ihsanguldur.coreledger.infrastructure.persistence.repository.LedgerBalanceRow;
import com.ihsanguldur.coreledger.infrastructure.persistence.repository.LedgerEntryJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class LedgerReconciliationJob {

    private final AccountJpaRepository accountJpaRepository;
    private final LedgerEntryJpaRepository ledgerEntryJpaRepository;

    @Scheduled(fixedDelayString = "${reconciliation.fixed-delay-ms:3600000}")
    public void run() {
        List<BalanceDiscrepancy> discrepancies = reconcile();

        if (discrepancies.isEmpty()) {
            log.info("ledger reconciliation: no discrepancies");
            return;
        }
        for (BalanceDiscrepancy d : discrepancies) {
            log.error("ledger reconciliation MISMATCH: account={} projected={} ledger={} diff={}",
                    d.accountId(), d.projectedBalance(), d.ledgerBalance(), d.difference());
        }
    }

    public List<BalanceDiscrepancy> reconcile() {
        Map<UUID, BigDecimal> ledgerBalances = ledgerEntryJpaRepository.sumSignedAmountByAccount().stream()
                .collect(Collectors.toMap(LedgerBalanceRow::getAccountId, LedgerBalanceRow::getBalance));

        List<BalanceDiscrepancy> discrepancies = new ArrayList<>();
        for (AccountJpaEntity account : accountJpaRepository.findAll()) {
            BigDecimal ledgerBalance = ledgerBalances.getOrDefault(account.getAccountId(), BigDecimal.ZERO);
            if (account.getBalance().compareTo(ledgerBalance) != 0) {
                discrepancies.add(new BalanceDiscrepancy(account.getAccountId(), account.getBalance(), ledgerBalance));
            }
        }
        return discrepancies;
    }

    public record BalanceDiscrepancy(UUID accountId, BigDecimal projectedBalance, BigDecimal ledgerBalance) {
        public BigDecimal difference() {
            return projectedBalance.subtract(ledgerBalance);
        }
    }
}
