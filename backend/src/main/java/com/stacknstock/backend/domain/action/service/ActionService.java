package com.stacknstock.backend.domain.action.service;

import com.stacknstock.backend.domain.action.dto.ActionRequest;
import com.stacknstock.backend.domain.action.dto.ActionResultResponse;
import com.stacknstock.backend.domain.action.entity.Action;
import com.stacknstock.backend.domain.action.enums.ActionType;
import com.stacknstock.backend.domain.action.repository.ActionRepository;
import com.stacknstock.backend.domain.day.entity.Day;
import com.stacknstock.backend.domain.day.entity.DayState;
import com.stacknstock.backend.domain.day.repository.DayRepository;
import com.stacknstock.backend.domain.day.repository.DayStateRepository;
import com.stacknstock.backend.domain.game.entity.GameRun;
import com.stacknstock.backend.domain.game.entity.RunState;
import com.stacknstock.backend.domain.game.enums.RunStatus;
import com.stacknstock.backend.domain.game.repository.GameRunRepository;
import com.stacknstock.backend.domain.game.repository.RunStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ActionService {

    private final GameRunRepository gameRunRepository;
    private final RunStateRepository runStateRepository;
    private final DayRepository dayRepository;
    private final DayStateRepository dayStateRepository;
    private final ActionRepository actionRepository;

    /**
     * 행동 실행 진입점
     *
     * 현재는 액션 타입에 따라 분기만 수행한다.
     * 실제 세부 로직은 다음 단계에서 각각 구현한다.
     */
    public ActionResultResponse executeAction(Long userId, ActionRequest request) {

        GameRun gameRun = gameRunRepository.findByUserUserIdAndStatus(userId, RunStatus.RUNNING)
                .orElseThrow(() -> new IllegalArgumentException("진행 중인 게임이 없습니다."));

        RunState runState = runStateRepository.findByRunRunId(gameRun.getRunId())
                .orElseThrow(() -> new IllegalStateException("RunState가 존재하지 않습니다."));

        Day day = dayRepository.findByGameRunRunIdAndDayNo(gameRun.getRunId(), runState.getCurrentDayNo())
                .orElseThrow(() -> new IllegalStateException("현재 Day가 존재하지 않습니다."));

        DayState dayState = dayStateRepository.findById(day.getDayId())
                .orElseThrow(() -> new IllegalStateException("DayState가 존재하지 않습니다."));

        ActionType actionType = request.actionType();

        return switch (actionType) {
            case INFO_PHONE -> executeInfoPhone(runState, dayState);
            case INFO_TV -> executeInfoTv(runState, dayState);
            case INFO_PAPER -> executeInfoPaper(runState, dayState);
            case STUDY -> executeStudy(gameRun, runState, day, dayState);
            case BUY -> executeBuy(runState, dayState, request);
            case SELL -> executeSell(runState, dayState, request);
            case SLEEP -> executeSleep(runState, dayState);
            case EVENT -> throw new IllegalArgumentException("EVENT는 직접 실행할 수 없는 타입입니다.");
        };
    }

    /**
     * 휴대폰 정보 보기
     */
    private ActionResultResponse executeInfoPhone(RunState runState, DayState dayState) {
        return new ActionResultResponse(
                runState.getCurrentDayNo(),
                dayState.getApRemaining(),
                runState.getCashBalance(),
                "INFO_PHONE 로직은 다음 단계에서 구현합니다."
        );
    }

    /**
     * TV 정보 보기
     */
    private ActionResultResponse executeInfoTv(RunState runState, DayState dayState) {
        return new ActionResultResponse(
                runState.getCurrentDayNo(),
                dayState.getApRemaining(),
                runState.getCashBalance(),
                "INFO_TV 로직은 다음 단계에서 구현합니다."
        );
    }

    /**
     * 신문 정보 보기
     */
    private ActionResultResponse executeInfoPaper(RunState runState, DayState dayState) {
        return new ActionResultResponse(
                runState.getCurrentDayNo(),
                dayState.getApRemaining(),
                runState.getCashBalance(),
                "INFO_PAPER 로직은 다음 단계에서 구현합니다."
        );
    }

    /**
     * 공부하기
     *
     * 규칙
     * - AP 1 사용
     * - studyCount +1
     * - Action 로그 기록
     */
    private ActionResultResponse executeStudy(
            GameRun run,
            RunState runState,
            Day day,
            DayState dayState
    ) {

        int apCost = 1;

        /* AP 부족 검사 */
        if (dayState.getApRemaining() < apCost) {
            throw new IllegalStateException("AP가 부족합니다.");
        }

        /* AP 차감 */
        dayState.setApRemaining(dayState.getApRemaining() - apCost);

        /* 공부 횟수 증가 */
        dayState.setStudyCount(dayState.getStudyCount() + 1);

        /* 행동 로그 저장 */
        Action action = Action.builder()
                .day(day)
                .run(run)
                .actionType(ActionType.STUDY)
                .apCost(apCost)
                .meta(null) // STUDY는 추가 데이터 없음
                .build();

        actionRepository.save(action);

        /* 결과 반환 */
        return new ActionResultResponse(
                runState.getCurrentDayNo(),
                dayState.getApRemaining(),
                runState.getCashBalance(),
                "공부를 진행했습니다."
        );
    }

    /**
     * 매수
     */
    private ActionResultResponse executeBuy(RunState runState, DayState dayState, ActionRequest request) {
        return new ActionResultResponse(
                runState.getCurrentDayNo(),
                dayState.getApRemaining(),
                runState.getCashBalance(),
                "BUY 로직은 다음 단계에서 구현합니다."
        );
    }

    /**
     * 매도
     */
    private ActionResultResponse executeSell(RunState runState, DayState dayState, ActionRequest request) {
        return new ActionResultResponse(
                runState.getCurrentDayNo(),
                dayState.getApRemaining(),
                runState.getCashBalance(),
                "SELL 로직은 다음 단계에서 구현합니다."
        );
    }

    /**
     * 잠 자기
     */
    private ActionResultResponse executeSleep(RunState runState, DayState dayState) {
        return new ActionResultResponse(
                runState.getCurrentDayNo(),
                dayState.getApRemaining(),
                runState.getCashBalance(),
                "SLEEP 로직은 다음 단계에서 구현합니다."
        );
    }
}