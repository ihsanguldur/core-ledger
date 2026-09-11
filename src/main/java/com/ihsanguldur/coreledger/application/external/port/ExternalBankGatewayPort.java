package com.ihsanguldur.coreledger.application.external.port;

import com.ihsanguldur.coreledger.domain.valueobject.Money;

public interface ExternalBankGatewayPort {
    boolean submitPayment(String destinationAccountRef, Money amount);
}
