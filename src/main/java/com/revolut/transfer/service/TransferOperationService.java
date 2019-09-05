package com.revolut.transfer.service;

import com.revolut.transfer.model.TransferTransaction;

import java.math.BigDecimal;

public interface TransferOperationService {

    TransferTransaction transfer(long fromId, long toId, BigDecimal amount);
}
