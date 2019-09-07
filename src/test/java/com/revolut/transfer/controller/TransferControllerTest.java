package com.revolut.transfer.controller;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.revolut.transfer.TransferApplication;
import com.revolut.transfer.controller.request.TransferRequest;
import com.revolut.transfer.model.Account;
import com.revolut.transfer.model.TransferTransaction;
import io.undertow.Undertow;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Integration testing for transfer api")
class TransferControllerTest {

    private static WebTarget target;
    private static UndertowJaxrsServer server;

    private static volatile Account firstAccount;
    private static volatile Account secondAccount;

    @BeforeAll
    static void init() {
        var application = new TransferApplication();

        server = new UndertowJaxrsServer();
        server.start(Undertow.builder().addHttpListener(8080, "localhost"));
        server.deploy(application);


        target = ClientBuilder.newClient()
                .register(JacksonJaxbJsonProvider.class)
                .target("http://localhost:8080");

        //prepare data
        var accountRepository = application.getApplicationContext().getAccountRepository();
        var accountA = new Account();
        accountA.setName("first one");
        accountA.setBalance(BigDecimal.valueOf(10000));
        firstAccount = accountRepository.create(accountA);

        var accountB = new Account();
        accountB.setName("second one");
        accountB.setBalance(BigDecimal.valueOf(1000));
        secondAccount = accountRepository.create(accountB);
    }

    @AfterAll
    static void shutdown() {
        server.stop();
    }

    @DisplayName("when request without fromId passed return 400")
    @Test
    void fromIdAbsentTest() {
        var transferRequest = new TransferRequest(null, secondAccount.getId(), BigDecimal.TEN);
        var response = target
                .path("api")
                .path("v1")
                .path("transfer")
                .request()
                .post(Entity.entity(transferRequest, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(400, response.getStatus());
    }

    @DisplayName("when request without toId passed return 400")
    @Test
    void toIdAbsentTest() {
        var transferRequest = new TransferRequest(firstAccount.getId(), null, BigDecimal.TEN);
        var response = target
                .path("api")
                .path("v1")
                .path("transfer")
                .request()
                .post(Entity.entity(transferRequest, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(400, response.getStatus());
    }

    @DisplayName("when request without amount passed return 400")
    @Test
    void amountAbsentTest() {
        var transferRequest = new TransferRequest(firstAccount.getId(), secondAccount.getId(), null);
        var response = target
                .path("api")
                .path("v1")
                .path("transfer")
                .request()
                .post(Entity.entity(transferRequest, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(400, response.getStatus());
    }

    @DisplayName("when request with negative amount passed return 400")
    @Test
    void negativeAmountTest() {
        var transferRequest = new TransferRequest(firstAccount.getId(), secondAccount.getId(), BigDecimal.valueOf(-10));
        var response = target
                .path("api")
                .path("v1")
                .path("transfer")
                .request()
                .post(Entity.entity(transferRequest, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(400, response.getStatus());
    }

    @DisplayName("when request with zero amount passed return 400")
    @Test
    void zeroAmountTest() {
        var transferRequest = new TransferRequest(firstAccount.getId(), secondAccount.getId(), BigDecimal.ZERO);
        var response = target
                .path("api")
                .path("v1")
                .path("transfer")
                .request()
                .post(Entity.entity(transferRequest, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(400, response.getStatus());
    }

    @DisplayName("when request with equal fromId and toId passed return 400")
    @Test
    void equalIdTest() {
        var transferRequest = new TransferRequest(firstAccount.getId(), firstAccount.getId(), BigDecimal.TEN);
        var response = target
                .path("api")
                .path("v1")
                .path("transfer")
                .request()
                .post(Entity.entity(transferRequest, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(400, response.getStatus());
    }

    @DisplayName("when request with amount higher than from balance passed return 400")
    @Test
    void highAmountTest() {
        var transferRequest = new TransferRequest(firstAccount.getId(),
                secondAccount.getId(),
                BigDecimal.valueOf(1).add(firstAccount.getBalance()));
        var response = target
                .path("api")
                .path("v1")
                .path("transfer")
                .request()
                .post(Entity.entity(transferRequest, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(400, response.getStatus());
    }

    @DisplayName("when request with non-existing fromId passed return 404")
    @Test
    void nonExistFromTest() {
        var transferRequest = new TransferRequest(-1L, firstAccount.getId(), BigDecimal.TEN);
        var response = target
                .path("api")
                .path("v1")
                .path("transfer")
                .request()
                .post(Entity.entity(transferRequest, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(404, response.getStatus());
    }

    @DisplayName("when request with non-existing toId passed return 404")
    @Test
    void nonExistToTest() {
        var transferRequest = new TransferRequest(firstAccount.getId(), -1L, BigDecimal.TEN);
        var response = target
                .path("api")
                .path("v1")
                .path("transfer")
                .request()
                .post(Entity.entity(transferRequest, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(404, response.getStatus());
    }

    @DisplayName("when request is valid perform transaction")
    @Test
    void validTransactionTest() {
        var fromInitialBalance = firstAccount.getBalance();
        var toInitialBalance = secondAccount.getBalance();

        var transferRequest = new TransferRequest(firstAccount.getId(), secondAccount.getId(), BigDecimal.TEN);
        var response = target
                .path("api")
                .path("v1")
                .path("transfer")
                .request()
                .post(Entity.entity(transferRequest, MediaType.APPLICATION_JSON_TYPE));

        assertEquals(200, response.getStatus());

        var transaction = response.readEntity(TransferTransaction.class);

        assertEquals(fromInitialBalance.subtract(transaction.getAmount()), firstAccount.getBalance());
        assertEquals(toInitialBalance.add(transaction.getAmount()), secondAccount.getBalance());
    }
}
