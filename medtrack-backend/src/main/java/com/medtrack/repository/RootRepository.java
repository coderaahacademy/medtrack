package com.medtrack.repository;

import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class RootRepository<T, ID> extends SimpleJpaRepository<T, ID> implements BaseRepository<T, ID> {
    private final String resourceName;

    public RootRepository(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.resourceName = entityInformation.getJavaType().getSimpleName();
    }

    @Override
    public T findByIdOrThrow(ID id) {
        return findById(id).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        resourceName + " not found with ID: " + id
                )
        );
    }
}
