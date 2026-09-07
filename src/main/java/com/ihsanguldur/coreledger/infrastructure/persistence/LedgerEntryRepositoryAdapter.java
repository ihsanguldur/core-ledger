package com.ihsanguldur.coreledger.infrastructure.persistence;

import com.ihsanguldur.coreledger.application.port.LedgerEntryRepository;
import com.ihsanguldur.coreledger.domain.entity.LedgerEntry;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import com.ihsanguldur.coreledger.infrastructure.persistence.mapper.LedgerEntryMapper;
import com.ihsanguldur.coreledger.infrastructure.persistence.repository.LedgerEntryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class LedgerEntryRepositoryAdapter implements LedgerEntryRepository {

    private final LedgerEntryJpaRepository ledgerEntryJpaRepository;

    @Override
    public List<LedgerEntry> findByAccountId(AccountId accountId) {
        return ledgerEntryJpaRepository.findByAccountIdOrderByCreatedAtAsc(accountId.value()).stream()
                .map(LedgerEntryMapper::toDomain)
                .toList();
    }
}
