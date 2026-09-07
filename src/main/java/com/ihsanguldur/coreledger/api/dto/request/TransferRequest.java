package com.ihsanguldur.coreledger.api.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        String currency
) {
}
