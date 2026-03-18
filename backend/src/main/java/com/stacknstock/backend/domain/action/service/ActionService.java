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
import com.stacknstock.backend.domain.scenario.entity.GameCase;
import com.stacknstock.backend.domain.scenario.entity.ScenarioDay;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stacknstock.backend.global.exception.BusinessException;
import com.stacknstock.backend.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
@Transactional
public class ActionService {

    private final GameRunRepository gameRunRepository;
    private final RunStateRepository runStateRepository;
    private final DayRepository dayRepository;
    private final DayStateRepository dayStateRepository;
    private final ActionRepository actionRepository;
    private final com.stacknstock.backend.domain.scenario.repository.ScenarioDayRepository scenarioDayRepository;
    private final com.stacknstock.backend.domain.scenario.repository.GameCaseRepository gameCaseRepository;

    /**
     * 행동 실행 진입점
     *
     * 현재는 액션 타입에 따라 분기만 수행한다.
     * 실제 세부 로직은 다음 단계에서 각각 구현한다.
     */
    public ActionResultResponse executeAction(Long userId, ActionRequest request) {

        GameRun gameRun = gameRunRepository.findByUserUserIdAndStatus(userId, RunStatus.RUNNING)
                .orElseThrow(() -> new BusinessException(ErrorCode.RUN_NOT_FOUND));

        RunState runState = runStateRepository.findByRunRunId(gameRun.getRunId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RUN_STATE_NOT_FOUND));

        Day day = dayRepository.findByGameRunRunIdAndDayNo(gameRun.getRunId(), runState.getCurrentDayNo())
                .orElseThrow(() -> new BusinessException(ErrorCode.DAY_NOT_FOUND));

        DayState dayState = dayStateRepository.findById(day.getDayId())
                .orElseThrow(() -> new BusinessException(ErrorCode.DAY_STATE_NOT_FOUND));

        ActionType actionType = request.actionType();

        return switch (actionType) {
            case INFO_PHONE -> executeInfoPhone(gameRun, runState, day, dayState);
            case INFO_TV -> executeInfoTv(gameRun, runState, day, dayState);
            case INFO_PAPER -> executeInfoPaper(gameRun, runState, day, dayState);
            case STUDY -> executeStudy(gameRun, runState, day, dayState);
            case SLEEP -> executeSleep(runState, dayState);
            case EVENT -> throw new BusinessException(ErrorCode.ACTION_NOT_ALLOWED);
        };
    }

    /**
     * 휴대폰 정보 보기
     * 규칙
     * - AP 소모 없음
     * - 오늘의 ScenarioDay → GameCase 조회
     * - phone 기사 반환
     * - Action 로그 기록
     */
    private ActionResultResponse executeInfoPhone(
            GameRun run,
            RunState runState,
            Day day,
            DayState dayState
    ) {

        /* 오늘의 시나리오 조회 */
        ScenarioDay scenarioDay = scenarioDayRepository
                .findWithGameCase(run.getRunId(), runState.getCurrentDayNo())
                .orElseThrow(() -> new BusinessException(ErrorCode.SCENARIO_NOT_FOUND));

        /* GameCase 조회 */
        GameCase gameCase = scenarioDay.getGameCase();
        if (gameCase == null) {
            throw new BusinessException(ErrorCode.GAME_CASE_NOT_FOUND);
        }

        /* 행동 로그 저장 (AP 소모 없음) */
        Action action = Action.builder()
                .day(day)
                .run(run)
                .actionType(ActionType.INFO_PHONE)
                .apCost(0)
                .meta("{\"case_id\": " + gameCase.getCaseId() + "}")
                .build();

        actionRepository.save(action);

        return new ActionResultResponse(
                runState.getCurrentDayNo(),
                dayState.getApRemaining(),
                runState.getCashBalance(),
                gameCase.getPhone()
        );
    }

    /**
     * TV 정보 보기
     * 규칙
     * - AP 1 사용
     * - 오늘의 ScenarioDay → GameCase 조회
     * - tv 기사 반환
     * - Action 로그 기록
     */
    private ActionResultResponse executeInfoTv(
            GameRun run,
            RunState runState,
            Day day,
            DayState dayState
    ) {

        int apCost = 1;

        if (dayState.getApRemaining() < apCost) {
            throw new BusinessException(ErrorCode.AP_NOT_ENOUGH);
        }

        dayState.setApRemaining(dayState.getApRemaining() - apCost);

        /* DayState 스냅샷 저장 */
        dayStateRepository.save(dayState);

        ScenarioDay scenarioDay = scenarioDayRepository
                .findWithGameCase(run.getRunId(), runState.getCurrentDayNo())
                .orElseThrow(() -> new BusinessException(ErrorCode.SCENARIO_NOT_FOUND));

        GameCase gameCase = scenarioDay.getGameCase();
        if (gameCase == null) {
            throw new BusinessException(ErrorCode.GAME_CASE_NOT_FOUND);
        }

        Action action = Action.builder()
                .day(day)
                .run(run)
                .actionType(ActionType.INFO_TV)
                .apCost(apCost)
                .meta("{\"case_id\": " + gameCase.getCaseId() + "}")
                .build();

        actionRepository.save(action);

        return new ActionResultResponse(
                runState.getCurrentDayNo(),
                dayState.getApRemaining(),
                runState.getCashBalance(),
                gameCase.getTv()
        );
    }

    /**
     * 신문 정보 보기
     * 규칙
     * - AP 2 사용
     * - 오늘의 ScenarioDay → GameCase 조회
     * - newspaper 기사 반환
     * - Action 로그 기록
     */
    private ActionResultResponse executeInfoPaper(
            GameRun run,
            RunState runState,
            Day day,
            DayState dayState
    ) {

        int apCost = 2;

        if (dayState.getApRemaining() < apCost) {
            throw new BusinessException(ErrorCode.AP_NOT_ENOUGH);
        }

        dayState.setApRemaining(dayState.getApRemaining() - apCost);

        /* DayState 스냅샷 저장 */
        dayStateRepository.save(dayState);

        ScenarioDay scenarioDay = scenarioDayRepository
                .findWithGameCase(run.getRunId(), runState.getCurrentDayNo())
                .orElseThrow(() -> new BusinessException(ErrorCode.SCENARIO_NOT_FOUND));

        GameCase gameCase = scenarioDay.getGameCase();
        if (gameCase == null) {
            throw new BusinessException(ErrorCode.GAME_CASE_NOT_FOUND);
        }

        Action action = Action.builder()
                .day(day)
                .run(run)
                .actionType(ActionType.INFO_PAPER)
                .apCost(apCost)
                .meta("{\"case_id\": " + gameCase.getCaseId() + "}")
                .build();

        actionRepository.save(action);

        return new ActionResultResponse(
                runState.getCurrentDayNo(),
                dayState.getApRemaining(),
                runState.getCashBalance(),
                gameCase.getNewspaper()
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
            throw new BusinessException(ErrorCode.AP_NOT_ENOUGH);
        }

        /* AP 차감 */
        dayState.setApRemaining(dayState.getApRemaining() - apCost);

        /* 공부 여부 기록 (이제 DayState는 studied Boolean 필드를 사용한다) */
        if (Boolean.TRUE.equals(dayState.getStudyDone())) {
            throw new BusinessException(ErrorCode.ALREADY_STUDIED);
        }

        dayState.setStudyDone(true);

        /* DayState 스냅샷 저장 */
        dayStateRepository.save(dayState);

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
     * 잠 자기
     */
    private ActionResultResponse executeSleep(RunState runState, DayState dayState) {

        /* 다음 날로 이동 */
        int nextDay = runState.getCurrentDayNo() + 1;
        runState.setCurrentDayNo(nextDay);

        /* RunState 스냅샷 저장 */
        runStateRepository.save(runState);

        /* 다음 Day 조회 */
        Day nextDayEntity = dayRepository
                .findByGameRunRunIdAndDayNo(runState.getRun().getRunId(), nextDay)
                .orElseGet(() -> dayRepository.save(
                        Day.create(runState.getRun(), nextDay, 2)
                ));

        /*
         * 다음 DayState 조회 또는 생성
         *
         * DayState는 각 일차의 상태를 저장하는 스냅샷 테이블이므로,
         * 다음 날로 처음 이동하는 시점에는 아직 row가 없을 수 있다.
         * 따라서 없으면 새로 생성해야 한다.
         */
        DayState nextDayState = dayStateRepository
                .findById(nextDayEntity.getDayId())
                .orElseGet(() -> DayState.create(nextDayEntity, 2, false));

        /*
         * 이미 존재하는 경우에도 하루 시작 상태로 맞춘다.
         * - AP 초기화
         * - 공부 여부 초기화
         */
        nextDayState.setApRemaining(2);
        nextDayState.setStudyDone(false);

        dayStateRepository.save(nextDayState);

        return new ActionResultResponse(
                runState.getCurrentDayNo(),
                nextDayState.getApRemaining(),
                runState.getCashBalance(),
                "다음 날로 이동했습니다."
        );
    }
}