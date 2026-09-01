package com.ihsanguldur.coreledger.domain.valueobject;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountIdTest {

    @Test
    void rejectsNullValue() {
        assertThatThrownBy(() -> new AccountId(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void equalValuesAreEqual() {
        UUID uuid = UUID.randomUUID();
        assertThat(AccountId.of(uuid)).isEqualTo(AccountId.of(uuid));
    }

    @Test
    void differentTypesWithSameUUIDAreNotEqual() {
        UUID uuid = UUID.randomUUID();
        assertThat(AccountId.of(uuid)).isNotEqualTo(TransactionId.of(uuid));
    }

    @Test
    void generateProducesUniqueId() {
        assertThat(AccountId.generate()).isNotEqualTo(AccountId.generate());
    }
}
