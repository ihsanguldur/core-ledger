package com.ihsanguldur.coreledger.infrastructure.persistence.jpaentity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
public class AccountJpaEntity {

    @Id
    private UUID accountId;

    private String currency;

    private BigDecimal balance;

    @Version
    private Long version;

    @CreationTimestamp
    private Instant createdAt;
}
