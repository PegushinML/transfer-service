package com.revolut.transfer.service;


import com.revolut.transfer.model.Account;
import com.revolut.transfer.model.TransferTransaction;
import com.revolut.transfer.repository.Repository;
import com.revolut.transfer.service.exception.EntityNotExistsException;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("Transfer operation service tests")
class TransferOperationServiceImplTest {

    private TransferOperationServiceImpl transferOperationService;
    private Repository<Account> accountRepository;
    private Repository<TransferTransaction> transactionRepository;


    @BeforeEach
    @SuppressWarnings("unchecked")
    void prepareService() {
        accountRepository = mock(Repository.class);
        transactionRepository = mock(Repository.class);
        transferOperationService = new TransferOperationServiceImpl(accountRepository, transactionRepository);
    }

    @DisplayName("Transfer method test")
    @Nested
    class TransferTest {

        @DisplayName("when amount is less than zero throw exception")
        @Test
        void negativeAmountTest() {
            assertThrows(IllegalArgumentException.class,
                    () -> transferOperationService.transfer(1L, 2L, BigDecimal.valueOf(-10)));
        }

        @DisplayName("when amount is zero throw exception")
        @Test
        void zeroAmountTest() {
            assertThrows(IllegalArgumentException.class,
                    () -> transferOperationService.transfer(1L, 2L, BigDecimal.valueOf(0)));
        }

        @DisplayName("when amount is null throw exception")
        @Test
        void nullAmountTest() {
            assertThrows(IllegalArgumentException.class,
                    () -> transferOperationService.transfer(1L, 2L, null));
        }

        @DisplayName("when amount is positive")
        @Nested
        class WhenAmountIsPositive {

            private BigDecimal amount;
            private Long fromId;
            private Long toId;

            @BeforeEach
            void initParameters() {
                amount = BigDecimal.valueOf(ThreadLocalRandom.current().nextLong(100, 1000));
                fromId = ThreadLocalRandom.current().nextLong();
                toId = ThreadLocalRandom.current().nextLong();
            }

            @DisplayName("when fromId equals toId throw exception")
            @Test
            void equalIdsTest() {
                assertThrows(IllegalArgumentException.class,
                        () -> transferOperationService.transfer(fromId, fromId, amount));
            }

            @DisplayName("when from account not found throw exception")
            @Test
            void fromNotFoundTest() {
                given(accountRepository.get(fromId)).willReturn(Optional.empty());
                assertThrows(EntityNotExistsException.class,
                        () -> transferOperationService.transfer(fromId, toId, amount));
            }

            @DisplayName("when from account found")
            @Nested
            class WhenFromFound {

                private Account fromAccount;
                private BigDecimal fromAccountInitialBalance;

                @BeforeEach
                void prepareData() {
                    //make initial balance available to transfer
                    fromAccountInitialBalance = amount.add(BigDecimal.valueOf(ThreadLocalRandom.current().nextLong(10000L)));

                    fromAccount = new Account();
                    fromAccount.setId(fromId);
                    fromAccount.setBalance(fromAccountInitialBalance);
                    given(accountRepository.get(fromId)).willReturn(Optional.of(fromAccount));
                }

                @DisplayName("when to account not found throw exception")
                @Test
                void toNotFoundTest() {
                    given(accountRepository.get(toId)).willReturn(Optional.empty());
                    assertThrows(EntityNotExistsException.class,
                            () -> transferOperationService.transfer(fromId, toId, amount));
                }

                @DisplayName("when to account found")
                @Nested
                class WhenToFound {

                    private Account toAccount;
                    private BigDecimal toAccountInitialBalance;

                    @BeforeEach
                    void prepareData() {
                        toAccountInitialBalance = BigDecimal.valueOf(ThreadLocalRandom.current().nextLong(100000L));
                        toAccount = new Account();
                        toAccount.setId(toId);
                        toAccount.setBalance(toAccountInitialBalance);
                        given(accountRepository.get(toId)).willReturn(Optional.of(toAccount));
                    }

                    @DisplayName("when transfer amount is higher than from account balance throw exception")
                    @Test
                    void validateFromBalanceTest() {
                        fromAccount.setBalance(amount.subtract(BigDecimal.TEN));
                        assertThrows(IllegalArgumentException.class,
                                () -> transferOperationService.transfer(fromId, toId, amount));
                    }

                    @DisplayName("when transfer succeed return transfer object and mutate state")
                    @Test
                    void successTransferTest() {
                        given(transactionRepository.create(any(TransferTransaction.class)))
                                .willAnswer((Answer<TransferTransaction>) invocation -> invocation.getArgument(0));

                        var result = transferOperationService.transfer(fromId, toId, amount);

                        assertNotNull(result);
                        assertEquals(result.getAmount(), amount);
                        assertEquals(result.getFrom(), fromId);
                        assertEquals(result.getTo(), toId);
                        assertNotNull(result.getDateTime());

                        assertEquals(fromAccount.getBalance(), fromAccountInitialBalance.subtract(amount));
                        assertEquals(toAccount.getBalance(), toAccountInitialBalance.add(amount));
                    }
                }
            }
        }
    }

    @DisplayName("Transfer concurrent test")
    @Nested
    class TransferConcurrentTest {
        private Account fromAccount;
        private Account toAccount;
        private BigDecimal initialSummaryBalance;

        @BeforeEach
        void prepareData() {
            fromAccount = new Account();
            fromAccount.setId(1L);
            fromAccount.setBalance(BigDecimal.valueOf(1000000L));
            given(accountRepository.get(fromAccount.getId())).willReturn(Optional.of(fromAccount));

            toAccount = new Account();
            toAccount.setId(2L);
            toAccount.setBalance(BigDecimal.valueOf(1000000L));
            given(accountRepository.get(toAccount.getId())).willReturn(Optional.of(toAccount));

            initialSummaryBalance = fromAccount.getBalance().add(toAccount.getBalance());

            given(transactionRepository.create(any(TransferTransaction.class)))
                    .willAnswer((Answer<TransferTransaction>) invocation -> invocation.getArgument(0));
        }

        @DisplayName("when multiple valid transactions performed concurrently")
        @SneakyThrows
        @Test
        void multipleTransactionTest() {
            int threads = 10;
            int tasksPerThread = 10000;

            var executionService = Executors.newFixedThreadPool(threads);
            var latch = new CountDownLatch(1);

            var futures = new ArrayList<Future<TransferTransaction>>(threads * tasksPerThread);
            for (int i = 0; i < threads * tasksPerThread; i++) {
                var future = executionService.submit(() -> {
                    latch.await();
                    if (ThreadLocalRandom.current().nextBoolean()) {
                        return transferOperationService.transfer(fromAccount.getId(), toAccount.getId(), BigDecimal.TEN);
                    } else {
                        return transferOperationService.transfer(toAccount.getId(), fromAccount.getId(), BigDecimal.TEN);
                    }
                });
                futures.add(future);
            }

            latch.countDown();

            var summaryAmount = futures.stream()
                    .map(future -> {
                        try {
                            return future.get().getAmount();
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException();
                        }
                    })
                    .reduce(BigDecimal::add)
                    .orElse(BigDecimal.ZERO);

            assertEquals(BigDecimal.valueOf(threads * tasksPerThread * 10), summaryAmount);
            assertEquals(initialSummaryBalance, fromAccount.getBalance().add(toAccount.getBalance()));
        }
    }

}