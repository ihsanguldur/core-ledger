package com.ihsanguldur.coreledger.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record IdempotencyKey(UUID value) {

    public IdempotencyKey {
        Objects.requireNonNull(value, "value cannot be null");
    }

    public static IdempotencyKey of(UUID value) {
        return new IdempotencyKey(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
