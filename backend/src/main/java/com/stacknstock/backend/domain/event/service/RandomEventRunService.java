package com.stacknstock.backend.domain.event.service;

import com.stacknstock.backend.domain.day.entity.Day;
import com.stacknstock.backend.domain.day.repository.DayRepository;
import com.stacknstock.backend.domain.event.entity.RandomEvent;
import com.stacknstock.backend.domain.event.entity.RandomEventRun;
import com.stacknstock.backend.domain.event.repository.RandomEventRepository;
import com.stacknstock.backend.domain.event.repository.RandomEventRunRepository;
import com.stacknstock.backend.domain.game.entity.GameRun;
import com.stacknstock.backend.domain.game.entity.RunState;
import com.stacknstock.backend.domain.game.repository.GameRunRepository;
import com.stacknstock.backend.domain.game.repository.RunStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RandomEventRunService {

    private final RandomEventRunRepository randomEventRunRepository;
    private final GameRunRepository gameRunRepository;
    private final DayRepository dayRepository;
    private final RandomEventRepository randomEventRepository;
    private final RunStateRepository runStateRepository;

    /**
     * 랜덤 이벤트 결과 기록 및 자산 반영
     */
    @Transactional
    public void recordEventResult(Long userId, Long runId, Long dayId, Long eventId, Boolean result, BigDecimal cashDelta) {

        GameRun gameRun = gameRunRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("진행 중인 게임을 찾을 수 없습니다. ID: " + runId));

        if (!gameRun.getUser().getUserId().equals(userId)) {
            throw new SecurityException("해당 게임에 대한 접근 권한이 없습니다.");
        }

        Day day = dayRepository.findById(dayId)
                .orElseThrow(() -> new IllegalArgumentException("해당 일차 정보를 찾을 수 없습니다. ID: " + dayId));

        RandomEvent event = randomEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("해당 이벤트를 찾을 수 없습니다. ID: " + eventId));

        RunState runState = runStateRepository.findByRunRunId(runId)
                .orElseThrow(() -> new IllegalStateException("게임 상태(RunState)가 존재하지 않습니다."));

        RandomEventRun eventRun = RandomEventRun.builder()
                .run(gameRun)
                .day(day)
                .event(event)
                .result(result)
                .cashDelta(cashDelta)
                .build();

        randomEventRunRepository.save(eventRun);

        runState.updateCashBalance(runState.getCashBalance().add(cashDelta));
    }
}