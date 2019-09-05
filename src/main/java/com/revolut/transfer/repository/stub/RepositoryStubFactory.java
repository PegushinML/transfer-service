package com.revolut.transfer.repository.stub;

import com.revolut.transfer.model.AbstractEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RepositoryStubFactory {

    public static <T extends AbstractEntity> RepositoryStub<T> initialize() {
        return new RepositoryStub<>();
    }
}
