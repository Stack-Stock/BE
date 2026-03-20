package com.stacknstock.backend.domain.game.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stacknstock.backend.domain.day.entity.Day;
import com.stacknstock.backend.domain.day.entity.DayState;
import com.stacknstock.backend.domain.day.repository.DayRepository;
import com.stacknstock.backend.domain.day.repository.DayStateRepository;
import com.stacknstock.backend.domain.event.entity.RandomEvent;
import com.stacknstock.backend.domain.event.repository.RandomEventRepository;
import com.stacknstock.backend.domain.game.dto.ContinueRunResponse;
import com.stacknstock.backend.domain.game.dto.PortfolioResponse;
import com.stacknstock.backend.domain.game.dto.PortfolioStockResponse;
import com.stacknstock.backend.domain.game.dto.StartGameResponse;
import com.stacknstock.backend.domain.game.entity.GameRun;
import com.stacknstock.backend.domain.game.entity.RunState;
import com.stacknstock.backend.domain.game.enums.RunStatus;
import com.stacknstock.backend.domain.game.repository.GameRunRepository;
import com.stacknstock.backend.domain.game.repository.RunStateRepository;
import com.stacknstock.backend.domain.scenario.entity.GameCase;
import com.stacknstock.backend.domain.scenario.entity.ScenarioDay;
import com.stacknstock.backend.domain.scenario.repository.GameCaseRepository;
import com.stacknstock.backend.domain.scenario.repository.ScenarioDayRepository;
import com.stacknstock.backend.domain.stock.entity.StockPrice;
import com.stacknstock.backend.domain.stock.repository.StockPriceRepository;
import com.stacknstock.backend.domain.stock.entity.Stock;
import com.stacknstock.backend.domain.stock.repository.StockRepository;
import com.stacknstock.backend.domain.trade.entity.Holding;
import com.stacknstock.backend.domain.trade.repository.HoldingRepository;
import com.stacknstock.backend.domain.user.entity.User;
import com.stacknstock.backend.domain.user.repository.UserRepository;
import com.stacknstock.backend.global.exception.BusinessException;
import com.stacknstock.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameRunService {

    private static final BigDecimal INITIAL_CASH = BigDecimal.valueOf(1_000_000L); // 초기 자본
    private static final Integer INITIAL_DAY_NO = 1; // 시작 일차
    private static final Integer INITIAL_AP = 2; // 기본 AP
    private static final Integer INITIAL_STUDY_COUNT = 0; // 초기 공부 횟수
    private static final Integer INITIAL_INSPIRATION_COUNT = 0; // 초기 번뜩임 개수

    private final GameRunRepository gameRunRepository;
    private final RunStateRepository runStateRepository;
    private final DayRepository dayRepository;
    private final DayStateRepository dayStateRepository;
    private final UserRepository userRepository;
    private final HoldingRepository holdingRepository;
    private final StockPriceRepository stockPriceRepository;
    private final StockRepository stockRepository;
    private final GameCaseRepository gameCaseRepository;
    private final RandomEventRepository randomEventRepository;
    private final ScenarioDayRepository scenarioDayRepository;

    /**
    새 게임 런 시작하기
     */
    @Transactional
    public StartGameResponse startGame(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        /**
         * 기존 GameRun 종료 처리
         *
         * - 해당 유저의 모든 RUNNING 상태 게임을 ENDED로 변경
         */
        List<GameRun> runningRuns = gameRunRepository.findAllByUserUserIdAndStatus(userId, RunStatus.RUNNING);
        for (GameRun run : runningRuns) {
            run.updateStatus(RunStatus.ENDED);
        }

        // 새 게임런 생성
        GameRun gameRun = GameRun.create(user, RunStatus.RUNNING, INITIAL_CASH);
        gameRunRepository.save(gameRun);

        generateScenarios(gameRun);

        RunState runState = RunState.create(
                gameRun,
                INITIAL_DAY_NO,
                INITIAL_CASH,
                INITIAL_INSPIRATION_COUNT,
                INITIAL_STUDY_COUNT
        );
        runStateRepository.save(runState);

        // 새 일차 기록 생성
        Day day = Day.create(gameRun, INITIAL_DAY_NO, INITIAL_AP);
        dayRepository.save(day);

        /**
         * DayState 생성 및 초기화
         * - AP: INITIAL_AP
         * - studied: false (아직 공부 안함)
         */
        DayState dayState = DayState.builder()
                .day(day)
                .apRemaining(INITIAL_AP)
                .studyDone(false)
                .build();

        dayStateRepository.save(dayState);

        /**
         * 초기 주가 데이터 생성
         * - Day 1 기준으로 모든 종목의 가격을 생성
         * - stocks 테이블의 basePrice를 기준으로 설정
         */
        List<Stock> stocks = stockRepository.findAll();

        if (stocks.isEmpty()) {
            throw new BusinessException(ErrorCode.STOCK_PRICE_NOT_FOUND);
        }

        List<StockPrice> stockPrices = new ArrayList<>();

        for (Stock stock : stocks) {

            BigDecimal basePrice = stock.getStartPrice();

            if (basePrice == null) {
                throw new BusinessException(ErrorCode.STOCK_PRICE_NOT_FOUND);
            }

            StockPrice stockPrice = StockPrice.builder()
                    .run(gameRun)
                    .stock(stock)
                    .baseDate(INITIAL_DAY_NO)
                    .closePrice(basePrice)
                    .returnPct(BigDecimal.ZERO)
                    .build();

            stockPrices.add(stockPrice);
        }

        stockPriceRepository.saveAll(stockPrices);

        return new StartGameResponse(
                gameRun.getRunId(),
                day.getDayNo(),
                runState.getCashBalance().longValue(),
                dayState.getApRemaining()
        );
    }

    /**
    진행 중인 게임 정보 가져오기
     */
    @Transactional(readOnly = true)
    public ContinueRunResponse continueRun(Long userId) {

        GameRun gameRun = gameRunRepository
                .findByUserUserIdAndStatus(userId, RunStatus.RUNNING)
                .orElseThrow(() -> new BusinessException(ErrorCode.RUN_NOT_FOUND));

        RunState runState = runStateRepository
                .findByRunRunId(gameRun.getRunId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RUN_STATE_NOT_FOUND));

        Day day = dayRepository
                .findByGameRunRunIdAndDayNo(
                        gameRun.getRunId(),
                        runState.getCurrentDayNo()
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.DAY_NOT_FOUND));

        DayState dayState = dayStateRepository
                .findById(day.getDayId())
                .orElseThrow(() -> new BusinessException(ErrorCode.DAY_STATE_NOT_FOUND));

        return new ContinueRunResponse(
                gameRun.getRunId(),
                day.getDayNo(),
                runState.getCashBalance().longValue(),
                dayState.getApRemaining()
        );
    }


    /**
     * 현재 로그인한 사용자의 진행 중인 게임 포트폴리오를 조회한다.
     *
     * 처리 순서
     * 1. RUNNING 상태의 GameRun 조회
     * 2. RunState 조회 (현재 day, 현금)
     * 3. Holding 목록 조회
     * 4. 각 Holding에 대해 현재가 조회
     * 5. 평가금액 / 손익 계산
     * 6. 총 자산 계산 후 응답 DTO 반환
     */
    @Transactional(readOnly = true)
    public PortfolioResponse getCurrentPortfolio(Long userId) {

        // 1) 현재 진행 중인 게임 조회
        GameRun gameRun = gameRunRepository.findByUserUserIdAndStatus(userId, RunStatus.RUNNING)
                .orElseThrow(() -> new BusinessException(ErrorCode.RUN_NOT_FOUND));

        // 2) 현재 게임 상태 조회
        RunState runState = runStateRepository.findByRunRunId(gameRun.getRunId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RUN_STATE_NOT_FOUND));

        // 3) 현재 게임의 보유 주식 목록 조회
        List<Holding> holdings = holdingRepository.findHoldingsWithStock(gameRun.getRunId());

        /*
         * N+1 문제 방지
         * 기존 방식: holding 수만큼 StockPrice 조회 쿼리가 발생
         * 해결 방식: 현재 run + day 기준 모든 StockPrice를 한 번에 조회
         */

        List<Long> stockIds = holdings.stream()
                .map(h -> h.getStock().getStockId())
                .toList();

        if (stockIds.isEmpty()) {
            return new PortfolioResponse(
                    gameRun.getRunId(),
                    runState.getCurrentDayNo(),
                    runState.getCashBalance(),
                    runState.getCashBalance(),
                    List.of(),
                    List.of()
            );
        }

        List<StockPrice> prices = stockPriceRepository
                .findByRunRunIdAndBaseDateAndStockStockIdIn(
                        gameRun.getRunId(),
                        runState.getCurrentDayNo(),
                        stockIds
                );

        /* stockId → price 매핑 */
        Map<Long, StockPrice> priceMap = prices.stream()
                .collect(Collectors.toMap(
                        p -> p.getStock().getStockId(),
                        p -> p,
                        (p1, p2) -> Integer.compare(p1.getBaseDate(), p2.getBaseDate()) > 0 ? p1 : p2
                ));

        List<PortfolioStockResponse> holdingResponses = new ArrayList<>();

        // 총 평가 금액(현금 제외)
        BigDecimal totalEvaluationAmount = BigDecimal.ZERO;

        for (Holding holding : holdings) {

            StockPrice stockPrice = priceMap.get(holding.getStock().getStockId());

            if (stockPrice == null) {
                throw new BusinessException(ErrorCode.STOCK_PRICE_NOT_FOUND);
            }

            BigDecimal currentPrice = stockPrice.getClosePrice();

            Long quantity = holding.getQty();
            BigDecimal avgCost = holding.getAvgCost();

            // 평가 금액 = 현재가 * 수량
            BigDecimal evaluationAmount = currentPrice.multiply(BigDecimal.valueOf(quantity));

            // 평가 손익 = (현재가 - 평균단가) * 수량
            BigDecimal profitLoss =
                    currentPrice.subtract(avgCost)
                            .multiply(BigDecimal.valueOf(quantity));

            totalEvaluationAmount = totalEvaluationAmount.add(evaluationAmount);

            holdingResponses.add(
                    new PortfolioStockResponse(
                            holding.getStock().getStockId(),
                            holding.getStock().getTicker(),
                            holding.getStock().getCompanyName(),
                            quantity,
                            avgCost,
                            currentPrice,
                            evaluationAmount,
                            profitLoss
                    )
            );
        }

        /**
         * 총 자산 계산
         * 총 자산 = 현금 + 보유 종목 평가금
         */
        BigDecimal cashBalance = runState.getCashBalance();
        BigDecimal totalAssetValue = cashBalance.add(totalEvaluationAmount);

        /**
         * 거래 로그는 현재 포트폴리오 API에서는 비어있는 리스트로 반환
         * (추후 TradeRepository 조회로 확장 가능)
         */
        List<com.stacknstock.backend.domain.trade.dto.TradeLogResponse> tradeLogs = List.of();

        /** 최종 응답 반환 */
        return new PortfolioResponse(
                gameRun.getRunId(),
                runState.getCurrentDayNo(),
                cashBalance,
                totalAssetValue,
                holdingResponses,
                tradeLogs
        );
    }

    private void generateScenarios(GameRun gameRun) {
        List<GameCase> allCases = gameCaseRepository.findAll();
        if (allCases.size() < 80) {
            throw new BusinessException(ErrorCode.GAME_CASE_NOT_FOUND);
        }
        Collections.shuffle(allCases);

        List<RandomEvent> eventPool = randomEventRepository.findAll();

        Map<Integer, RandomEvent> scheduledEvents = new HashMap<>();
        List<Integer> availableDays = new ArrayList<>();
        for (int i = 1; i <= 80; i++) availableDays.add(i);
        Collections.shuffle(availableDays);

        for (RandomEvent event : eventPool) {
            double roll = Math.random();
            double prob = event.getEventProbability().doubleValue();

            if (roll < prob) {
                if (!availableDays.isEmpty()) {
                    Integer targetDay = availableDays.remove(0);
                    scheduledEvents.put(targetDay, event);
                }
            }
        }

        List<ScenarioDay> scenarioDays = new ArrayList<>();
        for (int i = 0; i < 80; i++) {
            int dayNo = i + 1;
            GameCase gameCase = allCases.get(i);

            // 해당 날짜에 예약된 이벤트가 있으면 가져오고, 없으면 null
            RandomEvent selectedEvent = scheduledEvents.get(dayNo);

            ScenarioDay scenarioDay = ScenarioDay.create(
                    gameRun,
                    dayNo,
                    gameCase,
                    selectedEvent
            );
            scenarioDays.add(scenarioDay);
        }

        scenarioDayRepository.saveAll(scenarioDays);
    }

    /**
     * 게임런 최초 생성 시 80일의 주가 변동 데이터 자동 생성
     */
    private void generateStockPrices(GameRun gameRun) {

        List<Stock> stocks = stockRepository.findAll();

        if (stocks.isEmpty()) {
            throw new BusinessException(ErrorCode.STOCK_PRICE_NOT_FOUND);
        }

        // scenarioDay 미리 조회 (성능 최적화)
        List<ScenarioDay> scenarioDays = scenarioDayRepository
                .findByRunRunIdOrderByDayNoAsc(gameRun.getRunId());

        Map<Integer, ScenarioDay> scenarioMap = scenarioDays.stream()
                .collect(Collectors.toMap(
                        ScenarioDay::getDayNo,
                        s -> s
                ));

        // stock별 이전 가격 저장
        Map<Long, BigDecimal> prevPriceMap = new HashMap<>();

        List<StockPrice> allPrices = new ArrayList<>();

        // 1️⃣ Day 1 초기화
        for (Stock stock : stocks) {

            BigDecimal startPrice = stock.getStartPrice();

            if (startPrice == null) {
                throw new BusinessException(ErrorCode.STOCK_PRICE_NOT_FOUND);
            }

            prevPriceMap.put(stock.getStockId(), startPrice);

            allPrices.add(
                    StockPrice.builder()
                            .run(gameRun)
                            .stock(stock)
                            .baseDate(1)
                            .closePrice(startPrice)
                            .returnPct(BigDecimal.ZERO)
                            .build()
            );
        }

        // 2️⃣ Day 2 ~ 80 생성
        for (int day = 2; day <= 80; day++) {

            ScenarioDay scenarioDay = scenarioMap.get(day);

            // up_down_json 파싱 필요
            Map<Long, BigDecimal> returnMap = parseReturnMap(scenarioDay);

            for (Stock stock : stocks) {

                Long stockId = stock.getStockId();

                BigDecimal prevPrice = prevPriceMap.get(stockId);

                // 기본 랜덤 변동률
                BigDecimal returnPct = returnMap.getOrDefault(
                        stockId,
                        BigDecimal.valueOf((Math.random() - 0.5) * 0.02) // ±1%
                );

                BigDecimal newPrice = prevPrice.multiply(
                        BigDecimal.ONE.add(returnPct)
                );

                // 소수점 정리 (선택)
                newPrice = newPrice.setScale(0, BigDecimal.ROUND_HALF_UP);

                prevPriceMap.put(stockId, newPrice);

                allPrices.add(
                        StockPrice.builder()
                                .run(gameRun)
                                .stock(stock)
                                .baseDate(day)
                                .closePrice(newPrice)
                                .returnPct(returnPct)
                                .build()
                );
            }
        }

        stockPriceRepository.saveAll(allPrices);
    }

    /* Json 파싱 */
    private Map<Long, BigDecimal> parseReturnMap(ScenarioDay scenarioDay) {

        Map<Long, BigDecimal> result = new HashMap<>();

        if (scenarioDay == null || scenarioDay.getGameCase() == null) {
            return result;
        }

        String json = scenarioDay.getGameCase().getUpDownJson();

        if (json == null) return result;

        try {
            ObjectMapper mapper = new ObjectMapper();

            List<Map<String, Object>> list =
                    mapper.readValue(json, new TypeReference<>() {});

            for (Map<String, Object> item : list) {

                Long stockId = Long.valueOf(item.get("stock_id").toString());

                Object returnObj = item.get("game_return");

                if (returnObj != null) {
                    BigDecimal returnPct = new BigDecimal(returnObj.toString());
                    result.put(stockId, returnPct);
                }
            }

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }

        return result;
    }
}