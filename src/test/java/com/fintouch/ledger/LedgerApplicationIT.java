package com.fintouch.ledger;

import com.fintouch.ledger.infra.money.Money;
import com.fintouch.ledger.repository.UserRepository;
import com.fintouch.ledger.repository.WalletRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LedgerApplicationIT {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("fintouch_test")
            .withUsername("fintouch")
            .withPassword("fintouch");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @LocalServerPort
    int port;

    @Autowired
    UserRepository userRepository;

    @Autowired
    WalletRepository walletRepository;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        walletRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void transfer_debita_pagador_e_credita_recebedor() {
        Long payerId = createUser("Pagador", "payer@example.com", "DOC-PAYER", "COMMON");
        Long payeeId = createUser("Recebedor", "payee@example.com", "DOC-PAYEE", "MERCHANT");

        walletRepository.findByUserId(payerId).ifPresent(w -> {
            w.setBalance(Money.normalize(new BigDecimal("200.00")));
            walletRepository.save(w);
        });

        given()
                .contentType("application/json")
                .body("{\"value\":150.00,\"payer\":" + payerId + ",\"payee\":" + payeeId + "}")
                .when()
                .post("/transaction")
                .then()
                .statusCode(201)
                .body("transactionId", notNullValue())
                .body("value", equalTo(150.00f))
                .body("payer", equalTo(payerId.intValue()))
                .body("payee", equalTo(payeeId.intValue()))
                .body("createdAt", notNullValue());

        given()
                .when()
                .get("/wallet/{id}", payerId)
                .then()
                .statusCode(200)
                .body("balance", equalTo(50.00f));

        given()
                .when()
                .get("/wallet/{id}", payeeId)
                .then()
                .statusCode(200)
                .body("balance", equalTo(150.00f));
    }

    @Test
    void merchant_nao_pode_ser_pagador() {
        Long merchantPayerId = createUser("Lojista", "merchant@example.com", "DOC-MERCHANT", "MERCHANT");
        Long payeeId = createUser("Recebedor", "receiver@example.com", "DOC-RECEIVER", "COMMON");

        walletRepository.findByUserId(merchantPayerId).ifPresent(w -> {
            w.setBalance(Money.normalize(new BigDecimal("200.00")));
            walletRepository.save(w);
        });

        given()
                .contentType("application/json")
                .body("{\"value\":10.00,\"payer\":" + merchantPayerId + ",\"payee\":" + payeeId + "}")
                .when()
                .post("/transaction")
                .then()
                .statusCode(422)
                .body("code", equalTo("MERCHANT_CANNOT_PAY"));
    }

    private Long createUser(String fullName, String email, String document, String type) {
        return given()
                .contentType("application/json")
                .body("{\"fullName\":\"" + fullName + "\",\"email\":\"" + email + "\",\"document\":\"" + document + "\",\"type\":\"" + type + "\"}")
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }
}
