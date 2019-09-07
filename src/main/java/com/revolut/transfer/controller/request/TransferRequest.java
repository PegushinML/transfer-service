package com.revolut.transfer.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class TransferRequest {
    private final Long fromId;
    private final Long toId;
    private final BigDecimal amount;

    @JsonCreator
    public TransferRequest(@JsonProperty("fromId") Long fromId,
                           @JsonProperty("toId") Long toId,
                           @JsonProperty("amount") BigDecimal amount) {
        this.fromId = fromId;
        this.toId = toId;
        this.amount = amount;
    }
}
