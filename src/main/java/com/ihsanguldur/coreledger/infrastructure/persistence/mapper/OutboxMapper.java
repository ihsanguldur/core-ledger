package com.ihsanguldur.coreledger.infrastructure.persistence.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihsanguldur.coreledger.domain.event.DomainEvent;
import com.ihsanguldur.coreledger.domain.event.MoneyCredited;
import com.ihsanguldur.coreledger.domain.event.MoneyDebited;
import com.ihsanguldur.coreledger.infrastructure.persistence.jpaentity.OutboxJpaEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class OutboxMapper {

    private OutboxMapper() {}

    public static OutboxJpaEntity toJpaEntity(DomainEvent event, ObjectMapper mapper) {
        OutboxJpaEntity entity = new OutboxJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setAggregateId(aggregateIdOf(event));
        entity.setEventType(event.getClass().getSimpleName());
        entity.setCreatedAt(Instant.now());
        entity.setPayload(toJson(payloadOf(event), mapper));
        return entity;
    }

    private static UUID aggregateIdOf(DomainEvent event) {
        return switch (event) {
            case MoneyDebited e -> e.accountId().value();
            case MoneyCredited e -> e.accountId().value();
            default -> throw new IllegalArgumentException("Unknown domain event: " + event);
        };
    }

    private static Object payloadOf(DomainEvent event) {
        return switch (event) {
            case MoneyDebited e -> new MoneyEventPayload(
                    e.accountId().value(), e.amount().getAmount(), e.amount().getCurrency().getCurrencyCode(),
                    e.transactionId().value(), e.occurredAt()
            );
            case MoneyCredited e -> new MoneyEventPayload(
                    e.accountId().value(), e.amount().getAmount(), e.amount().getCurrency().getCurrencyCode(),
                    e.transactionId().value(), e.occurredAt()
            );
            default -> throw new IllegalArgumentException("Unknown domain event: " + event);
        };
    }

    private static String toJson(Object payload, ObjectMapper mapper) {
        try {
            return mapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to serialize domain event payload: " + payload, e);
        }
    }

    private record MoneyEventPayload(
            UUID accountId, BigDecimal amount, String currency, UUID transactionId, Instant occurredAt
    ) {}
}
