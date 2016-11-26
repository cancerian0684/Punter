package org.shunya.punter.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskDataRepository extends JpaRepository<TaskData, Long> {

    @Query("from TaskData t where t.process.id=?1 and t.active=true order by t.sequence")
    List<TaskData> findActiveTasks(long processId);

    @Query("from TaskData t where t.process.id=?1 order by t.sequence")
    List<TaskData> findTasks(long processId);
//    Optional<TaskData> findOneByMd5(String md5);
}
