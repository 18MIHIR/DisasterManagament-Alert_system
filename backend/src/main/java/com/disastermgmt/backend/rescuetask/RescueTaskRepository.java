package com.disastermgmt.backend.rescuetask;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RescueTaskRepository extends JpaRepository<RescueTask, Long> {

    List<RescueTask> findByResponderIdOrderByUpdatedAtDesc(Long responderId);

    List<RescueTask> findByDisasterId(Long disasterId);

    List<RescueTask> findByResponderIdAndTaskStatusIn(Long responderId, List<TaskStatus> statuses);

    @Query("SELECT rt FROM RescueTask rt WHERE rt.responder.id = :responderId " +
           "AND rt.taskStatus IN ('ASSIGNED', 'ACKNOWLEDGED', 'IN_PROGRESS')")
    List<RescueTask> findActiveTasksByResponder(@Param("responderId") Long responderId);
}
