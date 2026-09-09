package com.ihsanguldur.coreledger.infrastructure.persistence.jpaentity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox")
@Getter
@Setter
@NoArgsConstructor
public class OutboxJpaEntity {

    @Id
    private UUID id;

    private UUID aggregateId;

    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    private String payload;

    private Instant createdAt;

    private Instant publishedAt;
}
