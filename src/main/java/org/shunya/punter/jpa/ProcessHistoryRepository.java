package org.shunya.punter.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

public interface ProcessHistoryRepository extends JpaRepository<ProcessHistory, Long> {

//    Optional<TaskData> findOneByMd5(String md5);
    List<ProcessHistory> findByStartTimeLessThan(Date date);

    @Query("from ProcessHistory ph where ph.process.id = ?1 order by ph.id desc")
    List<ProcessHistory> findByProcessId(long id);

    @Query("from ProcessHistory ph where ph.process.username = ?1 AND ph.clearAlert=false order by ph.startTime desc")
    List<ProcessHistory> findByUsernameActive(String username);
}
