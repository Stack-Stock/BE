package com.stacknstock.backend.domain.game.service;

import com.stacknstock.backend.domain.day.entity.Day;
import com.stacknstock.backend.domain.day.entity.DayState;
import com.stacknstock.backend.domain.day.repository.DayRepository;
import com.stacknstock.backend.domain.day.repository.DayStateRepository;
import com.stacknstock.backend.domain.game.dto.ContinueRunResponse;
import com.stacknstock.backend.domain.game.dto.PortfolioResponse;
import com.stacknstock.backend.domain.game.dto.PortfolioStockResponse;
import com.stacknstock.backend.domain.game.dto.StartGameResponse;
import com.stacknstock.backend.domain.game.entity.GameRun;
import com.stacknstock.backend.domain.game.entity.RunState;
import com.stacknstock.backend.domain.game.enums.RunStatus;
import com.stacknstock.backend.domain.game.repository.GameRunRepository;
import com.stacknstock.backend.domain.game.repository.RunStateRepository;
import com.stacknstock.backend.domain.stock.entity.StockPrice;
import com.stacknstock.backend.domain.stock.repository.StockPriceRepository;
import com.stacknstock.backend.domain.trade.entity.Holding;
import com.stacknstock.backend.domain.trade.repository.HoldingRepository;
import com.stacknstock.backend.domain.user.entity.User;
import com.stacknstock.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final HoldingRepository holdingRepository;
    private final StockPriceRepository stockPriceRepository;

    /**
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

    /**
    진행 중인 게임 정보 가져오기
     */
    @Transactional(readOnly = true)
    public ContinueRunResponse continueRun(Long userId) {

        GameRun gameRun = gameRunRepository
                .findByUserUserIdAndStatus(userId, RunStatus.RUNNING)
                .orElseThrow(() -> new IllegalArgumentException("진행 중인 게임이 없습니다."));

        RunState runState = runStateRepository
                .findByRunRunId(gameRun.getRunId())
                .orElseThrow(() -> new IllegalStateException("RunState가 없습니다."));

        Day day = dayRepository
                .findByGameRunRunIdAndDayNo(
                        gameRun.getRunId(),
                        runState.getCurrentDayNo()
                )
                .orElseThrow(() -> new IllegalStateException("Day가 없습니다."));

        DayState dayState = dayStateRepository
                .findById(day.getDayId())
                .orElseThrow(() -> new IllegalStateException("DayState가 없습니다."));

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
                .orElseThrow(() -> new IllegalArgumentException("진행 중인 게임이 없습니다."));

        // 2) 현재 게임 상태 조회
        RunState runState = runStateRepository.findByRunRunId(gameRun.getRunId())
                .orElseThrow(() -> new IllegalStateException("RunState가 존재하지 않습니다."));

        // 3) 현재 게임의 보유 주식 목록 조회
        List<Holding> holdings = holdingRepository.findByRunRunId(gameRun.getRunId());

        /*
         * N+1 문제 방지
         * 기존 방식: holding 수만큼 StockPrice 조회 쿼리가 발생
         * 해결 방식: 현재 run + day 기준 모든 StockPrice를 한 번에 조회
         */

        List<Long> stockIds = holdings.stream()
                .map(h -> h.getStock().getStockId())
                .toList();

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
                        p -> p
                ));

        List<PortfolioStockResponse> holdingResponses = new ArrayList<>();

        // 총 평가 금액(현금 제외)
        long totalEvaluationAmount = 0L;

        for (Holding holding : holdings) {

            StockPrice stockPrice = priceMap.get(holding.getStock().getStockId());

            if (stockPrice == null) {
                throw new IllegalStateException("현재 주가 정보를 찾을 수 없습니다.");
            }

            // TODO
            // stock_price DB close_price 자료형 확인 필요
            long currentPrice = Long.parseLong(stockPrice.getClosePrice());

            long quantity = holding.getQty().longValue();
            long avgCost = holding.getAvgCost().longValue();

            // 평가 금액 = 현재가 * 수량
            long evaluationAmount = currentPrice * quantity;

            // 평가 손익 = (현재가 - 평균단가) * 수량
            long profitLoss = (currentPrice - avgCost) * quantity;

            totalEvaluationAmount += evaluationAmount;

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

        // 5) 총 자산 = 현금 + 보유 종목 평가금액
        long cashBalance = runState.getCashBalance().longValue();
        long totalAssetValue = cashBalance + totalEvaluationAmount;

        // 6) 최종 응답
        return new PortfolioResponse(
                gameRun.getRunId(),
                runState.getCurrentDayNo(),
                cashBalance,
                totalAssetValue,
                holdingResponses
        );
    }
}