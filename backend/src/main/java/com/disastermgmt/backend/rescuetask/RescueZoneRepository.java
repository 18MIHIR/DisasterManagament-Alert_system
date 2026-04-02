package com.disastermgmt.backend.rescuetask;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RescueZoneRepository extends JpaRepository<RescueZone, Long> {

    List<RescueZone> findAllByOrderByRegionAscNameAsc();
}
