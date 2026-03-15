package com.stacknstock.backend.domain.day.repository;

import com.stacknstock.backend.domain.day.entity.DayState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DayStateRepository extends JpaRepository<DayState, Long> {

    // Day ID로 일자 정보 찾기
    Optional<DayState> findById(Long dayId);

    /**
     * runId + dayNo 기준 DayState 조회
     *
     * DayState
     *  -> day
     *      -> gameRun
     *          -> runId
     *      -> dayNo
     */
    Optional<DayState> findByDayGameRunRunIdAndDayDayNo(Long runId, Integer dayNo);
}
