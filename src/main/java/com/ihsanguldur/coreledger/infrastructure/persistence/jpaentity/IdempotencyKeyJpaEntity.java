package com.ihsanguldur.coreledger.infrastructure.persistence.jpaentity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "idempotency_keys")
@Getter
@Setter
@NoArgsConstructor
public class IdempotencyKeyJpaEntity {

    @Id
    private UUID idempotencyKey;

    private Instant createdAt;
}
