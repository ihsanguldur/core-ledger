package com.ihsanguldur.coreledger.infrastructure.outbox;

import com.ihsanguldur.coreledger.application.DepositMoneyUseCase;
import com.ihsanguldur.coreledger.application.OpenAccountUseCase;
import com.ihsanguldur.coreledger.application.TransferMoneyUseCase;
import com.ihsanguldur.coreledger.domain.valueobject.AccountId;
import com.ihsanguldur.coreledger.domain.valueobject.IdempotencyKey;
import com.ihsanguldur.coreledger.domain.valueobject.Money;
import com.ihsanguldur.coreledger.infrastructure.persistence.jpaentity.OutboxJpaEntity;
import com.ihsanguldur.coreledger.infrastructure.persistence.repository.OutboxJpaRepository;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * outbox.relay.fixed-delay-ms çok büyük bir değere set edildi ki arka plandaki gerçek @Scheduled
 * tetiklenmesi testin ortasına girip "relay durmuş/pending birikmiş" varsayımını bozmasın —
 * relay burada sadece testin çağırdığı anda, manuel olarak çalıştırılıyor.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = "outbox.relay.fixed-delay-ms=3600000")
@Testcontainers
class OutboxRelayIT {

    private static final Currency USD = Currency.getInstance("USD");
    private static final String TOPIC = "ledger-events";

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Container
    @ServiceConnection
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    @Autowired
    private OpenAccountUseCase openAccountUseCase;

    @Autowired
    private DepositMoneyUseCase depositMoneyUseCase;

    @Autowired
    private TransferMoneyUseCase transferMoneyUseCase;

    @Autowired
    private OutboxJpaRepository outboxJpaRepository;

    @Autowired
    private OutboxRelay outboxRelay;

    @Test
    void eventsAccumulateWhileRelayIsStoppedThenPublishWithoutLossWhenRelayRuns() {
        AccountId sourceId = openAccountUseCase.open(USD).getAccountId();
        AccountId destinationId = openAccountUseCase.open(USD).getAccountId();
        depositMoneyUseCase.deposit(sourceId, Money.of(new BigDecimal("100.00"), USD));

        // "relay durmuş" halde transfer yapılıyor — event'ler outbox'ta birikmeli
        transferMoneyUseCase.transfer(
                sourceId, destinationId, Money.of(new BigDecimal("30.00"), USD), IdempotencyKey.of(UUID.randomUUID())
        );

        List<OutboxJpaEntity> pendingBeforeRelay = outboxJpaRepository.findByPublishedAtIsNullOrderByCreatedAtAsc();
        // deposit'in kendi MoneyCredited'ı (1) + transferin MoneyDebited + MoneyCredited'ı (2) = 3
        assertThat(pendingBeforeRelay).hasSize(3);

        // "relay tekrar başlıyor"
        outboxRelay.publishPendingEvents();

        assertThat(outboxJpaRepository.findByPublishedAtIsNullOrderByCreatedAtAsc()).isEmpty();
        assertThat(outboxJpaRepository.findAll())
                .allSatisfy(entry -> assertThat(entry.getPublishedAt()).isNotNull());

        List<ConsumerRecord<String, String>> records = consumeAllFrom(TOPIC);
        assertThat(records).hasSize(3);
        assertThat(records).extracting(ConsumerRecord::key)
                .contains(sourceId.value().toString(), destinationId.value().toString());
    }

    private List<ConsumerRecord<String, String>> consumeAllFrom(String topic) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "outbox-relay-it-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(List.of(topic));
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));

            List<ConsumerRecord<String, String>> result = new ArrayList<>();
            records.records(topic).forEach(result::add);
            return result;
        }
    }
}
