package com.revolut.transfer.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class Account extends AbstractEntity {
    private String name;
    private BigDecimal balance;
}
