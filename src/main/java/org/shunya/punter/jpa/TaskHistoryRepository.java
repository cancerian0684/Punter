package org.shunya.punter.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Long> {

//    Optional<TaskData> findOneByMd5(String md5);
}
