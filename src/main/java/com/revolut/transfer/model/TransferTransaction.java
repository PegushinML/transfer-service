package com.revolut.transfer.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class TransferTransaction extends AbstractEntity {
    private Long from;
    private Long to;
    private BigDecimal amount;
}
