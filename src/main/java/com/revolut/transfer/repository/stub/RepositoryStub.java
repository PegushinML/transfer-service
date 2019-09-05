package com.revolut.transfer.repository.stub;

import com.revolut.transfer.model.AbstractEntity;
import com.revolut.transfer.repository.Repository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class RepositoryStub<T extends AbstractEntity> implements Repository<T> {
    private final Map<Long, T> stateMap = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(0L);

    @Override
    public Optional<T> get(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(stateMap.get(id));
    }

    @Override
    public List<T> getAll() {
        return new ArrayList<>(stateMap.values());
    }

    @Override
    public T create(T entity) {
        if (entity.getId() != null)
            throw new IllegalArgumentException("Non created entity cannot have an id");
        var id = idCounter.incrementAndGet();
        entity.setId(id);
        var unexpected = stateMap.get(id);
        if (unexpected != null) {
            throw new IllegalStateException("Entity with id already exists");
        }
        stateMap.put(id, entity);
        return entity;
    }
}
