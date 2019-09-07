package com.revolut.transfer.controller;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.revolut.transfer.TransferApplication;
import com.revolut.transfer.controller.request.CreateAccountRequest;
import com.revolut.transfer.model.Account;
import io.undertow.Undertow;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.junit.jupiter.api.*;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Integration tests for account api")
class AccountControllerTest {

    private static WebTarget target;
    private static UndertowJaxrsServer server;

    @BeforeAll
    static void init() {
        var application = new TransferApplication();

        server = new UndertowJaxrsServer();
        server.start(Undertow.builder().addHttpListener(8080, "localhost"));
        server.deploy(application);

        target = ClientBuilder.newClient()
                .register(JacksonJaxbJsonProvider.class)
                .target("http://localhost:8080");
    }

    @AfterAll
    static void shutdown() {
        server.stop();
    }


    @DisplayName("/account POST method test")
    @Nested
    class CreateTest {

        @DisplayName("when empty request passed should return 403")
        @Test
        void emptyRequestTest() {
            var response = target
                    .path("api")
                    .path("v1")
                    .path("account")
                    .request()
                    .post(Entity.entity(new CreateAccountRequest(null, null), MediaType.APPLICATION_JSON_TYPE));

            assertEquals(400, response.getStatus());
        }

        @DisplayName("when request without name passed should return 403")
        @Test
        void withoutNameRequestTest() {
            var response = target
                    .path("api")
                    .path("v1")
                    .path("account")
                    .request()
                    .post(Entity.entity(new CreateAccountRequest(null, BigDecimal.ZERO), MediaType.APPLICATION_JSON_TYPE));

            assertEquals(400, response.getStatus());
        }

        @DisplayName("when request without balance passed should return 403")
        @Test
        void withoutBalanceRequestTest() {
            var response = target
                    .path("api")
                    .path("v1")
                    .path("account")
                    .request()
                    .post(Entity.entity(new CreateAccountRequest("QWERTY", null), MediaType.APPLICATION_JSON_TYPE));

            assertEquals(400, response.getStatus());
        }

        @DisplayName("when null request passed should return 403")
        @Test
        void nullRequestTest() {
            var response = target
                    .path("api")
                    .path("v1")
                    .path("account")
                    .request()
                    .post(Entity.entity(null, MediaType.APPLICATION_JSON_TYPE));

            assertEquals(400, response.getStatus());
        }

        @DisplayName("when request with invalid balance passed should return 403")
        @Test
        void negativeBalanceRequestTest() {
            var response = target
                    .path("api")
                    .path("v1")
                    .path("account")
                    .request()
                    .post(Entity.entity(new CreateAccountRequest("Qwerty", BigDecimal.valueOf(-100)), MediaType.APPLICATION_JSON_TYPE));

            assertEquals(400, response.getStatus());
        }

        @DisplayName("when valid request passed should create successfuly")
        @Test
        void successfulCreateTest() {
            var createRequest = new CreateAccountRequest("Qwerty", BigDecimal.valueOf(100));
            var response = target
                    .path("api")
                    .path("v1")
                    .path("account")
                    .request()
                    .post(Entity.entity(createRequest, MediaType.APPLICATION_JSON_TYPE));

            assertEquals(200, response.getStatus());
            var createdAccount = response.readEntity(Account.class);
            assertNotNull(createdAccount.getId());
            assertEquals(createdAccount.getName(), createRequest.getName());
            assertEquals(createdAccount.getBalance(), createRequest.getBalance());
        }
    }

    @DisplayName("/account/{id} GET method test")
    @Nested
    class GetByIdTest {

        @DisplayName("when account doesn't exist should return 404")
        @Test
        void notFoundTest() {
            var response = target
                    .path("api")
                    .path("v1")
                    .path("account")
                    .path("-1")
                    .request()
                    .get();

            assertEquals(404, response.getStatus());
        }

        @DisplayName("when account exists should return 404")
        @Test
        void foundExistingTest() {
            var createdAccount = createAccount();

            var response = target
                    .path("api")
                    .path("v1")
                    .path("account")
                    .path(createdAccount.getId().toString())
                    .request()
                    .get();

            var result = response.readEntity(Account.class);
            assertEquals(200, response.getStatus());
            assertEquals(createdAccount.getId(), result.getId());
            assertEquals(createdAccount.getName(), result.getName());
            assertEquals(createdAccount.getBalance(), result.getBalance());
        }

        private Account createAccount() {
            var createRequest = new CreateAccountRequest("Qwerty", BigDecimal.valueOf(100));
            var response = target
                    .path("api")
                    .path("v1")
                    .path("account")
                    .request()
                    .post(Entity.entity(createRequest, MediaType.APPLICATION_JSON_TYPE));
            return response.readEntity(Account.class);
        }
    }

    @DisplayName("/account GET method test")
    @Nested
    class GetAllTest {

        @DisplayName("when requested should return list og entities")
        @Test
        void getAllTest() {
            var response = target
                    .path("api")
                    .path("v1")
                    .path("account")
                    .request()
                    .get();

            assertEquals(200, response.getStatus());
            assertNotNull(response.getEntity());
        }
    }
}
