package com.ihsanguldur.coreledger.infrastructure.persistence.repository;

import java.math.BigDecimal;
import java.util.UUID;

public interface LedgerBalanceRow {
    UUID getAccountId();

    BigDecimal getBalance();
}
