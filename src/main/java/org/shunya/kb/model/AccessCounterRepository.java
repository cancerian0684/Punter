package org.shunya.kb.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccessCounterRepository extends JpaRepository<AccessCounter, Long> {

    Optional<AccessCounter> findOneByEntityIdAndEntityName(long id, String name);
}
