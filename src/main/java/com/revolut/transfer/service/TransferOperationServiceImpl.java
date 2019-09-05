package com.revolut.transfer.service;

import com.revolut.transfer.model.Account;
import com.revolut.transfer.model.TransferTransaction;
import com.revolut.transfer.repository.Repository;
import com.revolut.transfer.service.exception.EntityNotExistsException;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@RequiredArgsConstructor
public class TransferOperationServiceImpl implements TransferOperationService {

    private final Repository<Account> accountRepository;
    private final Repository<TransferTransaction> transactionRepository;

    @Override
    public TransferTransaction transfer(long fromId, long toId, BigDecimal amount) {
        //TODO: add concurrency support
        if (amount == null)
            throw new IllegalArgumentException("Amount cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Amount cannot be non-positive value");
        if (fromId == toId)
            throw new IllegalArgumentException("Unable to transfer to the same account");

        var fromAccount = accountRepository.get(fromId)
                .orElseThrow(() -> EntityNotExistsException.accountNotFoundById(fromId));
        var toAccount = accountRepository.get(toId)
                .orElseThrow(() -> EntityNotExistsException.accountNotFoundById(toId));

        if (fromAccount.getBalance().compareTo(amount) < 0)
            throw new IllegalArgumentException("Unable to transfer, balance it too low");

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        var transaction = new TransferTransaction();
        transaction.setFrom(fromId);
        transaction.setTo(toId);
        transaction.setAmount(amount);
        transaction.setDateTime(OffsetDateTime.now(ZoneOffset.UTC));

        return transactionRepository.create(transaction);
    }
}
