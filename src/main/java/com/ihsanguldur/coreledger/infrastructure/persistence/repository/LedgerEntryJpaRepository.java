package com.ihsanguldur.coreledger.infrastructure.persistence.repository;

import com.ihsanguldur.coreledger.infrastructure.persistence.jpaentity.LedgerEntryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface LedgerEntryJpaRepository extends JpaRepository<LedgerEntryJpaEntity, UUID> {

    List<LedgerEntryJpaEntity> findByAccountIdOrderByCreatedAtAsc(UUID accountId);

    @Query(value = """
            select account_id             as accountId,
                   sum(case when direction = 'CREDIT' then amount else -amount end) as balance
            from ledger_entries
            group by account_id
            """, nativeQuery = true)
    List<LedgerBalanceRow> sumSignedAmountByAccount();
}
