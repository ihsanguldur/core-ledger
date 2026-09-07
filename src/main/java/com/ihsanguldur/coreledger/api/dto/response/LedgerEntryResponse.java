package com.ihsanguldur.coreledger.api.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LedgerEntryResponse(
        UUID entryId, String direction, BigDecimal amount, String currency, UUID transactionId, Instant createdAt
) {
}