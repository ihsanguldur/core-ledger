package com.ihsanguldur.coreledger.api.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public record ExternalTransferRequest(
        UUID sourceAccountId, String destinationAccountRef, BigDecimal amount, String currency
) {
}
