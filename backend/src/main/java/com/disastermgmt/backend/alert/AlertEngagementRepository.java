package com.disastermgmt.backend.alert;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertEngagementRepository extends JpaRepository<AlertEngagement, Long> {
    
    @Query("SELECT COUNT(ae) FROM AlertEngagement ae WHERE ae.acknowledged = true")
    Long countAcknowledgedAlerts();

    @Query("SELECT COUNT(ae) FROM AlertEngagement ae WHERE ae.acknowledged = false")
    Long countIgnoredAlerts();
}
