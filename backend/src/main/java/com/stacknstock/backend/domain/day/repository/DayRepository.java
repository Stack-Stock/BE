package com.stacknstock.backend.domain.day.repository;

import com.stacknstock.backend.domain.day.entity.Day;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DayRepository extends JpaRepository<Day, Long> {

    // GameRun ID와 날짜(일차)로 Day 정보 찾기
    Optional<Day> findByGameRunRunIdAndDayNo(Long runId, Integer dayNo);
}
