package com.ihsanguldur.coreledger.application.port;

import com.ihsanguldur.coreledger.domain.valueobject.IdempotencyKey;

public interface IdempotencyKeyRepository {

    boolean exists(IdempotencyKey key);

    void save(IdempotencyKey key);
}
