package com.revolut.transfer.service;


import com.revolut.transfer.model.Account;
import com.revolut.transfer.model.TransferTransaction;
import com.revolut.transfer.repository.Repository;
import com.revolut.transfer.service.exception.EntityNotExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

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

}