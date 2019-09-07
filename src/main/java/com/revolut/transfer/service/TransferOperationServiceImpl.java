package com.revolut.transfer.service;

import com.revolut.transfer.model.Account;
import com.revolut.transfer.model.TransferTransaction;
import com.revolut.transfer.repository.Repository;
import com.revolut.transfer.service.exception.EntityNotExistsException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class TransferOperationServiceImpl implements TransferOperationService {

    private final Repository<Account> accountRepository;
    private final Repository<TransferTransaction> transactionRepository;
    private final Map<Long, Lock> lockMap;
    private final ReentrantLock lockObtainingLock;

    public TransferOperationServiceImpl(Repository<Account> accountRepository,
                                        Repository<TransferTransaction> transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.lockMap = new ConcurrentHashMap<>();
        this.lockObtainingLock = new ReentrantLock();
    }

    @Override
    public TransferTransaction transfer(long fromId, long toId, BigDecimal amount) {
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

        var fromLock = retrieveLock(fromId);
        var toLock = retrieveLock(toId);

        try {
            var unlocked = true;
            while (unlocked) {
                fromLock.lock();
                if (toLock.tryLock()) {
                    unlocked = false;
                } else {
                    fromLock.unlock();
                }
            }

            if (fromAccount.getBalance().compareTo(amount) < 0) {
                throw new IllegalArgumentException("Unable to transfer, balance is too low");
            }


            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
            toAccount.setBalance(toAccount.getBalance().add(amount));

            var transaction = new TransferTransaction();
            transaction.setFrom(fromId);
            transaction.setTo(toId);
            transaction.setAmount(amount);
            transaction.setDateTime(OffsetDateTime.now(ZoneOffset.UTC));

            return transactionRepository.create(transaction);
        } finally {
            fromLock.unlock();
            toLock.unlock();
        }
    }

    private Lock retrieveLock(long accountId) {
        var lock = lockMap.get(accountId);
        if (lock != null) return lock;
        lockObtainingLock.lock();
        try {
            return lockMap.computeIfAbsent(accountId, (id) -> new ReentrantLock());
        } finally {
            lockObtainingLock.unlock();
        }
    }
}
