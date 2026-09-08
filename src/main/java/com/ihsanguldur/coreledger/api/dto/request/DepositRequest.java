package com.ihsanguldur.coreledger.api.dto.request;

import java.math.BigDecimal;

public record DepositRequest(BigDecimal amount, String currency) {
}
