package com.ihsanguldur.coreledger.infrastructure.outbox;

import com.ihsanguldur.coreledger.infrastructure.persistence.jpaentity.OutboxJpaEntity;
import com.ihsanguldur.coreledger.infrastructure.persistence.repository.OutboxJpaRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxRelay {

    private static final String TOPIC = "ledger-events";

    private final OutboxJpaRepository outboxJpaRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelayString = "${outbox.relay.fixed-delay-ms:2000}")
    public void publishPendingEvents() {
        List<OutboxJpaEntity> pending = outboxJpaRepository.findByPublishedAtIsNullOrderByCreatedAtAsc();

        for (OutboxJpaEntity entry : pending) {
            ProducerRecord<String, String> record = new ProducerRecord<>(
                    TOPIC, entry.getAggregateId().toString(), entry.getPayload()
            );
            record.headers().add("eventType", entry.getEventType().getBytes(StandardCharsets.UTF_8));

            kafkaTemplate.send(record).join();

            entry.setPublishedAt(Instant.now());
            outboxJpaRepository.save(entry);
        }
    }
}
