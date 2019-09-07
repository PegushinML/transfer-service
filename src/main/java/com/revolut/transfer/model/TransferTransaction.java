package com.revolut.transfer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
public class TransferTransaction extends AbstractEntity {
    private Long from;
    private Long to;
    private BigDecimal amount;
    @JsonIgnore
    private OffsetDateTime dateTime;
}
