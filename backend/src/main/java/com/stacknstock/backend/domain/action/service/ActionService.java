package com.stacknstock.backend.domain.action.service;

import com.stacknstock.backend.domain.action.dto.ActionRequest;
import com.stacknstock.backend.domain.action.dto.ActionResultResponse;
import com.stacknstock.backend.domain.action.entity.Action;
import com.stacknstock.backend.domain.action.enums.ActionType;
import com.stacknstock.backend.domain.action.repository.ActionRepository;
import com.stacknstock.backend.domain.day.entity.Day;
import com.stacknstock.backend.domain.day.entity.DayResult;
import com.stacknstock.backend.domain.day.entity.DayState;
import com.stacknstock.backend.domain.day.repository.DayRepository;
import com.stacknstock.backend.domain.day.repository.DayResultRepository;
import com.stacknstock.backend.domain.day.repository.DayStateRepository;
import com.stacknstock.backend.domain.game.entity.GameRun;
import com.stacknstock.backend.domain.game.entity.RunState;
import com.stacknstock.backend.domain.game.enums.RunStatus;
import com.stacknstock.backend.domain.game.repository.GameRunRepository;
import com.stacknstock.backend.domain.game.repository.RunStateRepository;
import com.stacknstock.backend.domain.trade.entity.Holding;
import com.stacknstock.backend.domain.trade.repository.HoldingRepository;
import com.stacknstock.backend.domain.scenario.entity.GameCase;
import com.stacknstock.backend.domain.scenario.entity.ScenarioDay;
import com.stacknstock.backend.domain.stock.entity.StockPrice;
import com.stacknstock.backend.domain.stock.repository.StockPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stacknstock.backend.global.exception.BusinessException;
import com.stacknstock.backend.global.exception.ErrorCode;

import java.math.BigDecimal;
import java.util.List;

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
    private final DayResultRepository dayResultRepository;
    private final HoldingRepository holdingRepository;
    private final StockPriceRepository stockPriceRepository;

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

        /* 현재 run, day 정보 */
        GameRun run = runState.getRun();
        int currentDay = runState.getCurrentDayNo();

        /* 이미 저장된 경우 중복 방지 */
        if (!dayResultRepository.existsByRunRunIdAndDayNo(run.getRunId(), currentDay)) {

            /* 보유 주식 조회 */
            List<Holding> holdings = holdingRepository.findHoldingsWithStock(run.getRunId());

            /* 현재 가격 조회 */
            List<StockPrice> prices = stockPriceRepository
                    .findByRunRunIdAndBaseDateAndStockStockIdIn(
                            run.getRunId(),
                            currentDay,
                            holdings.stream().map(h -> h.getStock().getStockId()).toList()
                    );

            /* 가격 map 구성 */
            var priceMap = prices.stream()
                    .collect(java.util.stream.Collectors.toMap(
                            p -> p.getStock().getStockId(),
                            p -> p
                    ));

            /* 주식 평가금 계산 */
            BigDecimal stockValue = holdings.stream()
                    .map(h -> {
                        StockPrice price = priceMap.get(h.getStock().getStockId());
                        if (price == null) return BigDecimal.ZERO;
                        return price.getClosePrice().multiply(BigDecimal.valueOf(h.getQty()));
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal cash = runState.getCashBalance();
            BigDecimal totalAsset = cash.add(stockValue);

            /* DayResult 저장 */
            DayResult result = DayResult.builder()
                    .run(run)
                    .dayNo(currentDay)
                    .cashBalance(cash)
                    .stockValue(stockValue)
                    .totalAsset(totalAsset)
                    .build();

            dayResultRepository.save(result);
        }

        /* 다음 날로 이동 */
        int nextDay = currentDay + 1;
        runState.setCurrentDayNo(nextDay);

        runStateRepository.save(runState);

        /* 다음 Day 조회 또는 생성 */
        Day nextDayEntity = dayRepository
                .findByGameRunRunIdAndDayNo(run.getRunId(), nextDay)
                .orElseGet(() -> dayRepository.save(
                        Day.create(run, nextDay, 2)
                ));

        /* DayState 조회 또는 생성 */
        DayState nextDayState = dayStateRepository
                .findById(nextDayEntity.getDayId())
                .orElseGet(() -> DayState.create(nextDayEntity, 2, false));

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