package com.stacknstock.backend.domain.game.service;

import com.stacknstock.backend.domain.day.entity.DayResult;
import com.stacknstock.backend.domain.day.repository.DayResultRepository;
import com.stacknstock.backend.domain.game.dto.DaySummaryResponse;
import com.stacknstock.backend.domain.game.dto.PortfolioStockResponse;

import java.util.Comparator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.stacknstock.backend.domain.day.entity.DayState;
import com.stacknstock.backend.domain.day.repository.DayStateRepository;
import com.stacknstock.backend.domain.game.dto.ArticleArchiveResponse;
import com.stacknstock.backend.domain.game.dto.ArticleJsonDto;
import com.stacknstock.backend.domain.game.dto.ArticleResponse;
import com.stacknstock.backend.domain.game.dto.DailyStartResponse;
import com.stacknstock.backend.domain.game.dto.PortfolioResponse;
import com.stacknstock.backend.domain.game.entity.RunState;
import com.stacknstock.backend.domain.game.repository.RunStateRepository;
import com.stacknstock.backend.domain.scenario.entity.GameCase;
import com.stacknstock.backend.domain.scenario.entity.ScenarioDay;
import com.stacknstock.backend.domain.scenario.repository.ScenarioDayRepository;
import com.stacknstock.backend.domain.stock.entity.StockPrice;
import com.stacknstock.backend.domain.stock.repository.StockPriceRepository;
import com.stacknstock.backend.domain.trade.dto.PricePointResponse;
import com.stacknstock.backend.domain.trade.dto.TradingScreenResponse;
import com.stacknstock.backend.domain.trade.dto.TradingStockResponse;
import com.stacknstock.backend.domain.trade.entity.Holding;
import com.stacknstock.backend.domain.trade.entity.Trade;
import com.stacknstock.backend.domain.trade.repository.HoldingRepository;
import com.stacknstock.backend.domain.trade.repository.TradeRepository;
import com.stacknstock.backend.global.exception.BusinessException;
import com.stacknstock.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DailyStartService {

    private final RunStateRepository runStateRepository;
    private final ScenarioDayRepository scenarioDayRepository;
    private final HoldingRepository holdingRepository;
    private final StockPriceRepository stockPriceRepository;
    private final TradeRepository tradeRepository;
    private final DayStateRepository dayStateRepository;
    private final DayResultRepository dayResultRepository;

    /** JSON 파싱용 ObjectMapper */
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    /**
     * 하루 시작 API
     *
     * 상태 변경이 포함되는 로직:
     * - 번뜩임 지급
     * - T+3 정산
     *
     * 조회 전용 로직:
     * - 기사 아카이브
     * - 포트폴리오
     * - 거래 화면
     */
    @Transactional
    public DailyStartResponse getDailyStart(Long runId) {

        RunState runState = runStateRepository
                .findByRunRunId(runId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RUN_STATE_NOT_FOUND));

        Integer dayNo = runState.getCurrentDayNo();

        DayState today = dayStateRepository
                .findByDayGameRunRunIdAndDayDayNo(runId, dayNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.DAY_STATE_NOT_FOUND));

        DayState yesterday = dayStateRepository
                .findByDayGameRunRunIdAndDayDayNo(runId, dayNo - 1)
                .orElse(null);

        /* 상태 변경 로직 */
        boolean hasInspiration = processInspiration(runState, today, yesterday);
        BigDecimal settlement = processSettlement(runId, dayNo, runState);

        /* 조회용 데이터 구성 */
        List<Holding> holdings = holdingRepository.findHoldingsWithStock(runId);
        // 최신 가격 (현재 상태용)
        List<StockPrice> latestPrices = stockPriceRepository.findLatestPrices(runId, dayNo);
        // 전체 가격 히스토리 (그래프용)
        List<StockPrice> historyPrices = stockPriceRepository.findPriceHistory(runId);

        if (latestPrices.isEmpty()) {
            throw new BusinessException(ErrorCode.STOCK_PRICE_NOT_FOUND);
        }

        ArticleArchiveResponse articleArchive = buildArticleArchive(runId, dayNo);
        PortfolioResponse portfolio = buildPortfolio(runId, dayNo, runState, holdings, latestPrices);
        TradingScreenResponse tradingScreen = buildTradingScreen(runState, holdings, latestPrices, historyPrices);

        /**
         * 오늘 랜덤 이벤트 ID 조회
         */
        Long randomEventId = scenarioDayRepository
                .findEventIdByRunIdAndDayNo(runId, dayNo)
                .orElse(null);

        DaySummaryResponse daySummary = buildDaySummary(
                runId,
                dayNo,
                runState,
                holdings,
                latestPrices
        );

        return new DailyStartResponse(
                daySummary,
                portfolio,
                articleArchive,
                tradingScreen,
                randomEventId,
                settlement,
                hasInspiration
        );
    }

    /**
     * 번뜩임 지급 처리
     *
     * 조건:
     * - 어제 공부했음
     * - 오늘 아직 공부 안 했음
     * - 누적 공부 횟수가 3의 배수
     */
    private boolean processInspiration(RunState runState, DayState today, DayState yesterday) {
        boolean granted = false;

        if (yesterday != null
                && Boolean.TRUE.equals(yesterday.getStudyDone())
                && !Boolean.TRUE.equals(today.getStudyDone())
                && runState.getTotalStudyCnt() % 3 == 0) {

            runState.setInspirationCount(runState.getInspirationCount() + 1);
            granted = true;
        }

        return granted;
    }

    /**
     * T+3 정산 처리
     *
     * - 오늘 정산 대상 매도 거래 조회
     * - execAmount 합산
     * - RunState 현금 반영
     * - trade.settledAt 업데이트
     */
    private BigDecimal processSettlement(Long runId, Integer dayNo, RunState runState) {

        List<Trade> settlements = tradeRepository.findTodaySettlementTrades(runId, dayNo);

        BigDecimal settlement = settlements.stream()
                .map(Trade::getExecAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (settlement.compareTo(BigDecimal.ZERO) > 0) {
            runState.setCashBalance(runState.getCashBalance().add(settlement));
            settlements.forEach(t -> t.setSettledAt(LocalDateTime.now()));
        }

        return settlement;
    }

    /**
     * 전날 결산 요약 생성
     *
     * - 전날 대비 현금 변화
     * - 전날 대비 주식 평가금 변화
     * - 전날 대비 총 자산 변화
     */
    private DaySummaryResponse buildDaySummary(
            Long runId,
            Integer dayNo,
            RunState runState,
            List<Holding> holdings,
            List<StockPrice> latestPrices
    ) {

        // 전날 확정 결과 조회
        DayResult yesterdayResult = dayResultRepository
                .findByRunRunIdAndDayNo(runId, dayNo - 1)
                .orElse(null);

        if (yesterdayResult == null) {
            return null;
        }

        // 전날 값
        BigDecimal yesterdayCash = yesterdayResult.getCashBalance();
        BigDecimal yesterdayStockValue = yesterdayResult.getStockValue();

        // 오늘 값
        BigDecimal todayCash = runState.getCashBalance();
        BigDecimal todayStockValue = calculateStockValue(holdings, latestPrices);

        // 변화량 계산
        BigDecimal cashDiff = todayCash.subtract(yesterdayCash);
        BigDecimal stockDiff = todayStockValue.subtract(yesterdayStockValue);
        BigDecimal totalDiff = cashDiff.add(stockDiff);

        return new DaySummaryResponse(
                yesterdayResult.getDayNo(),
                cashDiff,
                stockDiff,
                totalDiff
        );
    }

    /** 기사 아카이브 구성 */
    private ArticleArchiveResponse buildArticleArchive(Long runId, Integer dayNo) {

        List<ScenarioDay> articles = scenarioDayRepository.findArticleArchive(runId, dayNo);

        List<ArticleResponse> articleResponses = articles.stream()
                .map(this::toArticleResponse)
                .toList();

        return new ArticleArchiveResponse(articleResponses);
    }

    /** ScenarioDay → ArticleResponse 변환 */
    private ArticleResponse toArticleResponse(ScenarioDay scenarioDay) {

        GameCase gameCase = scenarioDay.getGameCase();

        if (gameCase == null || gameCase.getArticleJson() == null) {
            throw new BusinessException(ErrorCode.GAME_CASE_NOT_FOUND);
        }

        try {
            List<ArticleJsonDto> articleList = objectMapper.readValue(
                    gameCase.getArticleJson(),
                    new TypeReference<List<ArticleJsonDto>>() {}
            );

            if (articleList.isEmpty()) {
                throw new BusinessException(ErrorCode.GAME_CASE_NOT_FOUND);
            }

            ArticleJsonDto article = articleList.get(0);

            return new ArticleResponse(
                    gameCase.getCaseId(),
                    article.title(),
                    article.publishedAt(),
                    article.url(),
                    gameCase.getStory(),
                    scenarioDay.getDayNo(),
                    gameCase.getStock().getStockId()
            );

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace(); // 또는 log.error
            throw new BusinessException(ErrorCode.JSON_PARSE_ERROR);
        }
    }

    /** 포트폴리오 응답 구성 */
    private PortfolioResponse buildPortfolio(Long runId, Integer dayNo, RunState runState, List<Holding> holdings, List<StockPrice> prices) {

        Map<Long, StockPrice> latestPriceMap = buildLatestPriceMap(prices);

        List<PortfolioStockResponse> holdingResponses = holdings.stream()
                .map(h -> {
                    StockPrice latestPrice = latestPriceMap.get(h.getStock().getStockId());

                    if (latestPrice == null) {
                        throw new BusinessException(ErrorCode.STOCK_PRICE_NOT_FOUND);
                    }

                    BigDecimal currentPrice = latestPrice.getClosePrice();
                    BigDecimal evaluationAmount = currentPrice.multiply(BigDecimal.valueOf(h.getQty()));
                    BigDecimal totalCost = h.getAvgCost().multiply(BigDecimal.valueOf(h.getQty()));
                    BigDecimal profitLoss = evaluationAmount.subtract(totalCost);

                    return new PortfolioStockResponse(
                            h.getStock().getStockId(),
                            h.getStock().getTicker(),
                            h.getStock().getCompanyName(),
                            h.getQty(),
                            h.getAvgCost(),
                            currentPrice,
                            evaluationAmount,
                            profitLoss
                    );
                })
                .toList();

        BigDecimal stockValue = holdingResponses.stream()
                .map(PortfolioStockResponse::evaluationAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAsset = runState.getCashBalance().add(stockValue);

        return new PortfolioResponse(
                runId,
                dayNo,
                runState.getCashBalance(),
                totalAsset,
                holdingResponses,
                List.of()
        );
    }

    /** 거래 화면 응답 구성 */
    private TradingScreenResponse buildTradingScreen(RunState runState, List<Holding> holdings, List<StockPrice> latestPrices, List<StockPrice> historyPrices) {

        Map<Long, List<StockPrice>> groupedHistoryPrices = historyPrices.stream()
                .collect(Collectors.groupingBy(p -> p.getStock().getStockId()));

        Map<Long, List<PricePointResponse>> priceHistoryMap = groupedHistoryPrices.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .sorted(Comparator.comparing(StockPrice::getBaseDate))
                                .map(p -> new PricePointResponse(p.getBaseDate(), p.getClosePrice()))
                                .toList()
                ));

        Map<Long, Long> holdingQtyMap = holdings.stream()
                .collect(Collectors.toMap(
                        h -> h.getStock().getStockId(),
                        Holding::getQty,
                        Long::sum
                ));

        /* 종목별 최신 가격 1건만 남김 */
        Map<Long, StockPrice> latestPriceMap = buildLatestPriceMap(latestPrices);

        List<TradingStockResponse> stocks = latestPriceMap.values().stream()
                .map(price -> {
                    Long stockId = price.getStock().getStockId();
                    BigDecimal changeAmount = calculateChangeAmount(price, groupedHistoryPrices.getOrDefault(stockId, List.of()));

                    return new TradingStockResponse(
                            stockId,
                            price.getStock().getCompanyName(),
                            holdingQtyMap.getOrDefault(stockId, 0L),
                            price.getClosePrice(),
                            price.getReturnPct(),
                            changeAmount,
                            priceHistoryMap.getOrDefault(stockId, List.of())
                    );
                })
                .toList();

        BigDecimal stockValue = calculateStockValue(holdings, latestPrices);
        BigDecimal totalAsset = runState.getCashBalance().add(stockValue);

        return new TradingScreenResponse(
                runState.getCashBalance(),
                stockValue,
                totalAsset,
                stocks
        );
    }

    /**
     * 보유 주식 평가금 계산
     *
     * 평균단가가 아니라 현재가 기준으로 계산해야 한다.
     */
    private BigDecimal calculateStockValue(List<Holding> holdings, List<StockPrice> prices) {

        Map<Long, StockPrice> latestPriceMap = buildLatestPriceMap(prices);

        return holdings.stream()
                .map(h -> {
                    StockPrice latestPrice = latestPriceMap.get(h.getStock().getStockId());

                    if (latestPrice == null) {
                        throw new BusinessException(ErrorCode.STOCK_PRICE_NOT_FOUND);
                    }

                    return latestPrice.getClosePrice().multiply(BigDecimal.valueOf(h.getQty()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<Long, StockPrice> buildLatestPriceMap(List<StockPrice> prices) {
        return prices.stream()
                .collect(Collectors.toMap(
                        p -> p.getStock().getStockId(),
                        p -> p,
                        (a, b) -> Integer.compare(a.getBaseDate(), b.getBaseDate()) > 0 ? a : b
                ));
    }

    private BigDecimal calculateChangeAmount(StockPrice latestPrice, List<StockPrice> stockPrices) {
        if (stockPrices == null || stockPrices.isEmpty()) {
            return BigDecimal.ZERO;
        }

        List<StockPrice> sorted = stockPrices.stream()
                .sorted(Comparator.comparing(StockPrice::getBaseDate))
                .toList();

        for (int i = 0; i < sorted.size(); i++) {
            StockPrice current = sorted.get(i);
            if (current.getBaseDate().equals(latestPrice.getBaseDate()) && i > 0) {
                BigDecimal prevPrice = sorted.get(i - 1).getClosePrice();
                return latestPrice.getClosePrice().subtract(prevPrice);
            }
        }

        return BigDecimal.ZERO;
    }
}
