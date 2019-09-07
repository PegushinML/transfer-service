package com.revolut.transfer;

import com.revolut.transfer.controller.AccountController;
import com.revolut.transfer.controller.TransferController;
import com.revolut.transfer.controller.exception.EntityNotExistsExceptionMapper;
import com.revolut.transfer.controller.exception.IllegalArgumentExceptionMapper;
import com.revolut.transfer.model.Account;
import com.revolut.transfer.model.TransferTransaction;
import com.revolut.transfer.repository.Repository;
import com.revolut.transfer.repository.stub.RepositoryStubFactory;
import com.revolut.transfer.service.TransferOperationService;
import com.revolut.transfer.service.TransferOperationServiceImpl;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class TransferApplicationContext {

    private final Repository<Account> accountRepository;
    private final Repository<TransferTransaction> transferTransactionRepository;

    private final TransferOperationService transferOperationService;

    private final AccountController accountController;
    private final TransferController transferController;

    private final EntityNotExistsExceptionMapper entityNotExistsExceptionMapper;
    private final IllegalArgumentExceptionMapper illegalArgumentExceptionMapper;

    static TransferApplicationContext init() {
        var accountRepository = RepositoryStubFactory.<Account>initialize();
        var transferTransactionRepository = RepositoryStubFactory.<TransferTransaction>initialize();

        var transferOperationService = new TransferOperationServiceImpl(accountRepository, transferTransactionRepository);

        var accountController = new AccountController(accountRepository);
        var transferController = new TransferController(transferOperationService);

        var entityNotExistsExceptionMapper = new EntityNotExistsExceptionMapper();
        var illegalArgumentExceptionMapper = new IllegalArgumentExceptionMapper();

        return TransferApplicationContext.builder()
                .accountRepository(accountRepository)
                .transferTransactionRepository(transferTransactionRepository)
                .transferOperationService(transferOperationService)
                .accountController(accountController)
                .transferController(transferController)
                .entityNotExistsExceptionMapper(entityNotExistsExceptionMapper)
                .illegalArgumentExceptionMapper(illegalArgumentExceptionMapper)
                .build();
    }

    public Set<Object> getBeans() {
        return Set.of(
                transferController,
                accountController,
                illegalArgumentExceptionMapper,
                entityNotExistsExceptionMapper
        );
    }
}
