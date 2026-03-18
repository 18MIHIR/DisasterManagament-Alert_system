package com.disastermgmt.backend.disaster;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DisasterRepository extends JpaRepository<Disaster, Long> {

    List<Disaster> findByStatus(DisasterStatus status);

    List<Disaster> findByType(DisasterType type);

    List<Disaster> findBySeverity(DisasterSeverity severity);

    List<Disaster> findByRegionContainingIgnoreCase(String region);

    List<Disaster> findByLocationContainingIgnoreCase(String location);

    Optional<Disaster> findByExternalIdAndSource(String externalId, String source);

    @Query("SELECT d FROM Disaster d WHERE d.status IN :statuses ORDER BY d.eventTime DESC")
    List<Disaster> findByStatusIn(@Param("statuses") List<DisasterStatus> statuses);

    @Query("SELECT d FROM Disaster d WHERE " +
           "(:type IS NULL OR d.type = :type) AND " +
           "(:severity IS NULL OR d.severity = :severity) AND " +
           "(:status IS NULL OR d.status = :status) AND " +
           "(:region IS NULL OR LOWER(d.region) LIKE LOWER(CONCAT('%', :region, '%'))) " +
           "ORDER BY d.eventTime DESC")
    List<Disaster> findWithFilters(
        @Param("type") DisasterType type,
        @Param("severity") DisasterSeverity severity,
        @Param("status") DisasterStatus status,
        @Param("region") String region
    );

    @Query("SELECT d FROM Disaster d WHERE d.eventTime >= :startTime ORDER BY d.eventTime DESC")
    List<Disaster> findRecentDisasters(@Param("startTime") LocalDateTime startTime);

    @Query("SELECT d FROM Disaster d WHERE d.status = 'ACTIVE' ORDER BY d.severity DESC, d.eventTime DESC")
    List<Disaster> findActiveDisastersOrderedBySeverity();

    @Query("SELECT d FROM Disaster d WHERE d.status = 'PENDING' ORDER BY d.createdAt ASC")
    List<Disaster> findPendingDisasters();

    @Query("SELECT COUNT(d) FROM Disaster d WHERE d.status = :status")
    Long countByStatus(@Param("status") DisasterStatus status);

    @Query("SELECT COUNT(d) FROM Disaster d WHERE d.type = :type AND d.status = 'ACTIVE'")
    Long countActiveByType(@Param("type") DisasterType type);

    @Modifying
    @Query("DELETE FROM Disaster d WHERE d.status = :status")
    int deleteByStatus(@Param("status") DisasterStatus status);
}
