package com.stacknstock.backend.domain.game.service;

import com.stacknstock.backend.domain.day.entity.Day;
import com.stacknstock.backend.domain.day.entity.DayState;
import com.stacknstock.backend.domain.day.repository.DayRepository;
import com.stacknstock.backend.domain.day.repository.DayStateRepository;
import com.stacknstock.backend.domain.game.dto.StartGameResponse;
import com.stacknstock.backend.domain.game.entity.GameRun;
import com.stacknstock.backend.domain.game.entity.RunState;
import com.stacknstock.backend.domain.game.enums.RunStatus;
import com.stacknstock.backend.domain.game.repository.GameRunRepository;
import com.stacknstock.backend.domain.game.repository.RunStateRepository;
import com.stacknstock.backend.domain.user.entity.User;
import com.stacknstock.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class GameRunService {

    private static final BigDecimal INITIAL_CASH = BigDecimal.valueOf(3_000_000L); // 초기 자본
    private static final int INITIAL_DAY_NO = 1; // 시작 일차
    private static final int INITIAL_AP = 2; // 기본 AP
    private static final int INITIAL_STUDY_COUNT = 0; // 초기 공부 횟수
    private static final int INITIAL_INSPIRATION_COUNT = 0; // 초기 번뜩임 개수

    private final GameRunRepository gameRunRepository;
    private final RunStateRepository runStateRepository;
    private final DayRepository dayRepository;
    private final DayStateRepository dayStateRepository;
    private final UserRepository userRepository;

    /*
    새 게임 런 시작하기
     */
    @Transactional
    public StartGameResponse startGame(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 새 게임런 생성
        GameRun gameRun = GameRun.create(user, RunStatus.RUNNING, INITIAL_CASH);
        gameRunRepository.save(gameRun);

        RunState runState = RunState.create(
                gameRun,
                INITIAL_DAY_NO,
                INITIAL_CASH,
                INITIAL_INSPIRATION_COUNT
        );
        runStateRepository.save(runState);

        // 새 일차 기록 생성
        Day day = Day.create(gameRun, INITIAL_DAY_NO, INITIAL_AP);
        dayRepository.save(day);

        DayState dayState = DayState.create(day, INITIAL_AP, INITIAL_STUDY_COUNT);
        dayStateRepository.save(dayState);

        return new StartGameResponse(
                gameRun.getRunId(),
                day.getDayNo(),
                runState.getCashBalance().longValue(),
                dayState.getApRemaining()
        );
    }
}