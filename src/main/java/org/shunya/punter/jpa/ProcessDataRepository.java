package org.shunya.punter.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProcessDataRepository extends JpaRepository<ProcessData, Long> {

    @Query("from ProcessData p where p.username=?1 order by p.id asc")
    List<ProcessData> findByUsername(String username);
}
