package com.revolut.transfer.service.exception;

public class EntityNotExistsException extends RuntimeException {

    private EntityNotExistsException(String message) {
        super(message);
    }

    public static EntityNotExistsException accountNotFoundById(long id) {
        return new EntityNotExistsException("Account was not found by id=" + id);
    }
}
