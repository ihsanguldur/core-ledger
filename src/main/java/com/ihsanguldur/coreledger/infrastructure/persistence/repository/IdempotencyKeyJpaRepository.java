package com.ihsanguldur.coreledger.infrastructure.persistence.repository;

import com.ihsanguldur.coreledger.infrastructure.persistence.jpaentity.IdempotencyKeyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IdempotencyKeyJpaRepository extends JpaRepository<IdempotencyKeyJpaEntity, UUID> {
}
