package com.ihsanguldur.coreledger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CoreLedgerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoreLedgerApplication.class, args);
    }
}