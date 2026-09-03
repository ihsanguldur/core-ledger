package com.ihsanguldur.coreledger.infrastructure.persistence.jpaentity;

import com.ihsanguldur.coreledger.domain.entity.LedgerEntry;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries")
@Getter
@Setter
@NoArgsConstructor
public class LedgerEntryJpaEntity {

    @Id
    private UUID entryId;

    private UUID accountId;

    @Enumerated(EnumType.STRING)
    private LedgerEntry.Direction direction;

    private BigDecimal amount;

    private String currency;

    private UUID transactionId;

    private Instant createdAt;
}
