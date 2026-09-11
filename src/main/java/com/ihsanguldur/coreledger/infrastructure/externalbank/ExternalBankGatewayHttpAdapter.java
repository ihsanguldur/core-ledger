package com.ihsanguldur.coreledger.infrastructure.externalbank;

import com.ihsanguldur.coreledger.application.external.exception.ExternalBankGatewayException;
import com.ihsanguldur.coreledger.application.external.port.ExternalBankGatewayPort;
import com.ihsanguldur.coreledger.domain.valueobject.Money;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class ExternalBankGatewayHttpAdapter implements ExternalBankGatewayPort {

    private final RestClient restClient;

    public ExternalBankGatewayHttpAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${external-bank-gateway.base-url}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public boolean submitPayment(String destinationAccountRef, Money amount) {
        try {
            PaymentResponse response = restClient.post()
                    .uri("/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new PaymentRequest(
                            destinationAccountRef, amount.getAmount(), amount.getCurrency().getCurrencyCode()
                    ))
                    .retrieve()
                    .body(PaymentResponse.class);

            return response != null && "ACCEPTED".equals(response.status());
        } catch (RestClientException e) {
            throw new ExternalBankGatewayException("external bank gateway call failed", e);
        }
    }

    private record PaymentRequest(String destinationAccountRef, BigDecimal amount, String currency) {
    }

    private record PaymentResponse(UUID externalReference, String status) {
    }
}
