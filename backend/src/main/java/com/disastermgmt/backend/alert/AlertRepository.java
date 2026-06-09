package com.disastermgmt.backend.alert;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByDisasterId(Long disasterId);

    List<Alert> findAllByOrderByBroadcastTimeDesc();

    List<Alert> findByRegionOrderByBroadcastTimeDesc(String region);

    List<Alert> findByRegionContainingIgnoreCaseOrderByBroadcastTimeDesc(String region);
}
