package com.revolut.transfer.controller.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class MessageContainer {
    private final String message;

    static MessageContainer from(RuntimeException ex) {
        return new MessageContainer(ex.getLocalizedMessage());
    }
}
