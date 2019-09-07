package com.revolut.transfer.repository.stub;

import com.revolut.transfer.model.Account;
import com.revolut.transfer.repository.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Repository stub")
class RepositoryStubTest {

    private Repository<Account> accountRepository;

    @DisplayName("is initialized via factory")
    @Test
    void initTest() {
        var repo = RepositoryStubFactory.<Account>initialize();
        assertNotNull(repo);
    }

    @DisplayName("when initialized")
    @Nested
    class WhenInitialized {

        @BeforeEach
        void initRepository() {
            accountRepository = RepositoryStubFactory.initialize();
        }

        @DisplayName("has no entities")
        @Test
        void emptyRepositoryTest() {
            var result = accountRepository.getAll();
            assertTrue(result.isEmpty());
        }

        @DisplayName("can create entity without id")
        @Test
        void canCreateWithoutIdTest() {
            var entity = new Account();
            var result = accountRepository.create(entity);
            assertNotNull(result);
            assertNotNull(result.getId());
        }

        @DisplayName("can not create entity with id")
        @Test
        void canNotCreateWithoutIdTest() {
            var entity = new Account();
            entity.setId(1L);
            assertThrows(IllegalArgumentException.class, () -> accountRepository.create(entity));
        }

        @DisplayName("can create multiple entities concurrently")
        @Test
        void canCreateConcurrently() {
            int threads = 10;
            int tasksPerThread = 10000;

            var executionService = Executors.newFixedThreadPool(threads);
            var latch = new CountDownLatch(1);

            var futures = new ArrayList<Future<Account>>(threads * tasksPerThread);
            for (int i = 0; i < threads * tasksPerThread; i++) {
                var future = executionService.submit(() -> {
                    latch.await();
                    var entity = new Account();
                    return accountRepository.create(entity);
                });
                futures.add(future);
            }
            latch.countDown();

            var accounts = futures.stream()
                    .map(accountFuture -> {
                        try {
                            return accountFuture.get();
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException();
                        }
                    })
                    .collect(Collectors.toSet());

            assertEquals(accounts.size(), threads * tasksPerThread);

            var uniqueIds = accounts.stream()
                    .map(Account::getId)
                    .collect(Collectors.toSet());

            assertEquals(uniqueIds.size(), threads * tasksPerThread);
        }

        @DisplayName("can get empty optional with null id")
        @Test
        void nullTest() {
            var result = accountRepository.get(null);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @DisplayName("after creating one entity")
        @Nested
        class WhenOneCreated {

            private Account createdAccount;

            @BeforeEach
            void createAccount() {
                createdAccount = accountRepository.create(new Account());
            }

            @DisplayName("can create new entity with another id")
            @Test
            void createNewWithAnotherIdTest() {
                var anotherAccount = accountRepository.create(new Account());
                assertNotEquals(anotherAccount, createdAccount);
                assertNotEquals(anotherAccount.getId(), createdAccount.getId());
            }

            @DisplayName("can get by id created entity")
            @Test
            void getTest() {
                var result = accountRepository.get(createdAccount.getId());
                assertTrue(result.isPresent());
                assertEquals(result.get(), createdAccount);
            }

            @DisplayName("getAll() returns one entity")
            @Test
            void getAllSingleTest() {
                var result = accountRepository.getAll();
                assertNotNull(result);
                assertEquals(result.size(), 1);
            }
        }

        @DisplayName("after creating multiple entities")
        @Nested
        class WhenMultipleCreated {
            private List<Account> entityList;

            @BeforeEach
            void createMultipleEntities() {
                entityList = IntStream.range(0, 1000)
                        .boxed()
                        .map(i -> new Account())
                        .collect(Collectors.toList());

                for (Account entity : entityList) {
                    accountRepository.create(entity);
                }
            }

            @DisplayName("getAll returns collection of entities equal to created entities amount")
            @Test
            void getAllSizeCheck() {
                var result = accountRepository.getAll();

                assertNotNull(result);
                assertEquals(result.size(), entityList.size());
            }
        }
    }

}