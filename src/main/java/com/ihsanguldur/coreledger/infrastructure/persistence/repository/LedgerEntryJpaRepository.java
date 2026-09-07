package com.ihsanguldur.coreledger.infrastructure.persistence.repository;

import com.ihsanguldur.coreledger.infrastructure.persistence.jpaentity.LedgerEntryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LedgerEntryJpaRepository extends JpaRepository<LedgerEntryJpaEntity, UUID> {
    List<LedgerEntryJpaEntity> findByAccountIdOrderByCreatedAtAsc(UUID accountId);
}