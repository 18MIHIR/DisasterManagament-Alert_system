package com.disastermgmt.backend.report;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findBySubmittedByIdOrderBySubmittedAtDesc(Long submittedById);

    List<Report> findByResponderIdOrderBySubmittedAtDesc(Long responderId);

    List<Report> findAllByOrderBySubmittedAtDesc();
}
