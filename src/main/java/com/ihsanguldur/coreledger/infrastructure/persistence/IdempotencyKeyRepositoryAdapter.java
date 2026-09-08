package com.ihsanguldur.coreledger.infrastructure.persistence;

import com.ihsanguldur.coreledger.application.port.IdempotencyKeyRepository;
import com.ihsanguldur.coreledger.domain.valueobject.IdempotencyKey;
import com.ihsanguldur.coreledger.infrastructure.persistence.jpaentity.IdempotencyKeyJpaEntity;
import com.ihsanguldur.coreledger.infrastructure.persistence.repository.IdempotencyKeyJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
@RequiredArgsConstructor
public class IdempotencyKeyRepositoryAdapter implements IdempotencyKeyRepository {

    private final IdempotencyKeyJpaRepository idempotencyKeyJpaRepository;


    @Override
    public boolean exists(IdempotencyKey key) {
        return idempotencyKeyJpaRepository.existsById(key.value());
    }

    @Override
    public void save(IdempotencyKey key) {
        IdempotencyKeyJpaEntity entity = new IdempotencyKeyJpaEntity();
        entity.setIdempotencyKey(key.value());
        entity.setCreatedAt(Instant.now());
        idempotencyKeyJpaRepository.save(entity);
    }
}
