package com.revolut.transfer.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Simple entity template to avoid unnecessary reflection usage in {@link com.revolut.transfer.repository.stub.RepositoryStub}
 */
@Getter
@Setter
public abstract class AbstractEntity {
    private Long id;
}
