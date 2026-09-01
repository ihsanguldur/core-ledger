package com.ihsanguldur.coreledger.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record EntryId(UUID value) {

    public EntryId {
        Objects.requireNonNull(value, "value cannot be null");
    }

    public static EntryId generate() {
        return new EntryId(UUID.randomUUID());
    }

    public static EntryId of(UUID value) {
        return new EntryId(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
