package com.ihsanguldur.coreledger.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OpenAccountResponse(UUID accountId, BigDecimal balance, String currency) {
}
