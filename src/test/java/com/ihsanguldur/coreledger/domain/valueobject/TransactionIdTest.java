package com.ihsanguldur.coreledger.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionIdTest {

    @Test
    void rejectsNullValue() {
        assertThatThrownBy(() -> new TransactionId(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void equalValuesAreEqual() {
        UUID uuid = UUID.randomUUID();
        assertThat(TransactionId.of(uuid)).isEqualTo(TransactionId.of(uuid));
    }

    @Test
    void differentTypesWithSameUUIDAreNotEqual() {
        UUID uuid = UUID.randomUUID();
        assertThat(TransactionId.of(uuid)).isNotEqualTo(AccountId.of(uuid));
    }

    @Test
    void generateProducesUniqueId() {
        assertThat(TransactionId.generate()).isNotEqualTo(TransactionId.generate());
    }
}
