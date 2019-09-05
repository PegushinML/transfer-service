package com.revolut.transfer.repository;

import java.util.List;
import java.util.Optional;

public interface Repository<T> {
    Optional<T> get(Long id);

    List<T> getAll();

    T create(T account);
}
