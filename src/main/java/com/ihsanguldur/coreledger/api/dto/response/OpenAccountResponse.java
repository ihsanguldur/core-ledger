package com.ihsanguldur.coreledger.api.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record OpenAccountResponse(UUID accountId, BigDecimal balance, String currency) {
}
