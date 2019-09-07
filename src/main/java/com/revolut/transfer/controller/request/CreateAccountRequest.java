package com.revolut.transfer.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class CreateAccountRequest {

    @JsonProperty("name")
    private final String name;
    @JsonProperty("balance")
    private final BigDecimal balance;

    @JsonCreator
    public CreateAccountRequest(@JsonProperty("name") String name,
                                @JsonProperty("balance") BigDecimal balance) {
        this.name = name;
        this.balance = balance;
    }
}
