package com.stacknstock.backend.domain.day.repository;

import com.stacknstock.backend.domain.day.entity.DayResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DayResultRepository extends JpaRepository<DayResult, Long> {

    Optional<DayResult> findByRunRunIdAndDayNo(Long runId, Integer dayNo);

    boolean existsByRunRunIdAndDayNo(Long runId, Integer dayNo);
}