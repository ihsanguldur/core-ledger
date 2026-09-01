package com.ihsanguldur.coreledger.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdempotencyKeyTest {

    @Test
    void rejectsNullValue() {
        assertThatThrownBy(() -> new IdempotencyKey(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void equalValuesAreEqual() {
        UUID uuid = UUID.randomUUID();
        assertThat(IdempotencyKey.of(uuid)).isEqualTo(IdempotencyKey.of(uuid));
    }

    @Test
    void differentTypesWithSameUUIDAreNotEqual() {
        UUID uuid = UUID.randomUUID();
        assertThat(IdempotencyKey.of(uuid)).isNotEqualTo(AccountId.of(uuid));
    }
}
