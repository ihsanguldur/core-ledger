package com.ihsanguldur.coreledger.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntryIdTest {

    @Test
    void rejectsNullValue() {
        assertThatThrownBy(() -> new EntryId(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void equalValuesAreEqual() {
        UUID uuid = UUID.randomUUID();
        assertThat(EntryId.of(uuid)).isEqualTo(EntryId.of(uuid));
    }

    @Test
    void differentTypesWithSameUUIDAreNotEqual() {
        UUID uuid = UUID.randomUUID();
        assertThat(EntryId.of(uuid)).isNotEqualTo(AccountId.of(uuid));
    }

    @Test
    void generateProducesUniqueId() {
        assertThat(EntryId.generate()).isNotEqualTo(EntryId.generate());
    }
}