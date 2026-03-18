package com.stacknstock.backend.domain.scenario.service;

import com.stacknstock.backend.domain.event.entity.RandomEvent;
import com.stacknstock.backend.domain.event.repository.RandomEventRepository;
import com.stacknstock.backend.domain.game.entity.GameRun;
import com.stacknstock.backend.domain.game.repository.GameRunRepository;
import com.stacknstock.backend.domain.scenario.entity.ScenarioDay;
import com.stacknstock.backend.domain.scenario.repository.ScenarioDayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScenarioDayService {

    private final ScenarioDayRepository scenarioDayRepository;
    private final RandomEventRepository randomEventRepository;
    private final GameRunRepository gameRunRepository; // 1. GameRun 조회를 위해 추가

    /**
     * 보안 확인: 해당 runId가 요청한 유저의 것인지 검증한다.
     */
    private void validateRunOwnership(Long runId, Long userId) {
        GameRun gameRun = gameRunRepository.findById(runId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게임 런을 찾을 수 없습니다."));

        if (!gameRun.getUser().getUserId().equals(userId)) {
            throw new SecurityException("해당 게임 런에 대한 수정 권한이 없습니다.");
        }
    }

    /**
     * 현재 날짜 기준으로 3일 뒤, 경찰 출두 이벤트를 발생시킨다.
     */
    @Transactional
    public void policeEvent(Long userId, Long runId, Integer currentDayNo) {
        validateRunOwnership(runId, userId);

        int targetDayNo = currentDayNo + 3;

        if (targetDayNo > 80) {
            return;
        }

        ScenarioDay targetScenario = scenarioDayRepository.findByRunRunIdAndDayNo(runId, targetDayNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 일차의 시나리오가 존재하지 않습니다. Day: " + targetDayNo));

        RandomEvent policeEvent = randomEventRepository.findById(8L)
                .orElseThrow(() -> new IllegalArgumentException("8번 이벤트(경찰 출두)를 찾을 수 없습니다."));

        targetScenario.updateEvent(policeEvent);
    }

    /**
     * 현재 날짜 기준으로 1, 2일 뒤, 죄책감 이벤트를 발생시킨다.
     */
    @Transactional
    public void guiltyEvent(Long userId, Long runId, Integer currentDayNo) {
        validateRunOwnership(runId, userId);

        RandomEvent guiltyEvent = randomEventRepository.findById(9L)
                .orElseThrow(() -> new IllegalArgumentException("9번 이벤트(죄책감)를 찾을 수 없습니다."));

        List<Integer> targetDays = List.of(currentDayNo + 1, currentDayNo + 2);

        for (int targetDay : targetDays) {
            if (targetDay > 80) {
                continue;
            }

            scenarioDayRepository.findByRunRunIdAndDayNo(runId, targetDay)
                    .ifPresent(scenario -> scenario.updateEvent(guiltyEvent));
        }
    }
}