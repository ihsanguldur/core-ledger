package com.ihsanguldur.coreledger.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI coreLedgerOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("core-ledger API")
                .description("Double-entry bookkeeping core banking ledger and money transfer service")
                .version("v0.1"));
    }
}