package com.stacknstock.backend.domain.game.service;

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
import com.stacknstock.backend.domain.trade.repository.HoldingRepository;
import com.stacknstock.backend.global.exception.BusinessException;
import com.stacknstock.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResumeService {

    private final RunStateRepository runStateRepository;
    private final DayStateRepository dayStateRepository;
    private final ScenarioDayRepository scenarioDayRepository;
    private final HoldingRepository holdingRepository;
    private final StockPriceRepository stockPriceRepository;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    /**
     * 이어하기 전용 조회 서비스
     *
     * 주의:
     * - 상태 변경 금지
     * - 번뜩임 지급 금지
     * - T+3 정산 금지
     */
    public DailyStartResponse getResumeData(Long runId) {

        RunState runState = runStateRepository
                .findByRunRunId(runId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RUN_STATE_NOT_FOUND));

        Integer dayNo = runState.getCurrentDayNo();

        DayState today = dayStateRepository
                .findByDayGameRunRunIdAndDayDayNo(runId, dayNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.DAY_STATE_NOT_FOUND));

        /*
         * 기사 아카이브
         */
        List<ScenarioDay> articles = scenarioDayRepository.findArticleArchive(runId, dayNo);

        List<ArticleResponse> articleResponses = articles.stream()
                .map(sd -> {
                    GameCase gameCase = sd.getGameCase();

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
                                sd.getDayNo(),
                                gameCase.getStock().getStockId()
                        );
                    } catch (BusinessException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new BusinessException(ErrorCode.JSON_PARSE_ERROR);
                    }
                })
                .toList();

        ArticleArchiveResponse articleArchive = new ArticleArchiveResponse(articleResponses);

        /*
         * 포트폴리오
         */
        List<Holding> holdings = holdingRepository.findHoldingsWithStock(runId);

        /*
         * 거래창 데이터
         *
         * 이어하기에서는 조회만 수행한다.
         */
        // 최신 가격 (현재 상태용)
        List<StockPrice> latestPrices = stockPriceRepository.findLatestPrices(runId, dayNo);

        // 전체 가격 히스토리 (그래프용)
        List<StockPrice> historyPrices = stockPriceRepository.findPriceHistory(runId);

        if (latestPrices.isEmpty()) {
            throw new BusinessException(ErrorCode.STOCK_PRICE_NOT_FOUND);
        }

        Map<Long, StockPrice> latestPriceMap = latestPrices.stream()
                .collect(Collectors.toMap(
                        p -> p.getStock().getStockId(),
                        p -> p,
                        (a, b) -> a.getBaseDate() > b.getBaseDate() ? a : b
                ));

        Map<Long, List<StockPrice>> groupedPrices = historyPrices.stream()
                .collect(Collectors.groupingBy(p -> p.getStock().getStockId()));

        /**
         * 보유 종목 DTO 구성 (updated)
         */
        var holdingResponses = holdings.stream()
                .map(h -> {
                    BigDecimal currentPrice = latestPriceMap.getOrDefault(
                            h.getStock().getStockId(), null
                    ) == null
                            ? BigDecimal.ZERO
                            : latestPriceMap.get(h.getStock().getStockId()).getClosePrice();

                    BigDecimal evaluationAmount = currentPrice.multiply(BigDecimal.valueOf(h.getQty()));
                    BigDecimal totalCost = h.getAvgCost().multiply(BigDecimal.valueOf(h.getQty()));
                    BigDecimal profitLoss = evaluationAmount.subtract(totalCost);

                    return new com.stacknstock.backend.domain.game.dto.PortfolioStockResponse(
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

        /**
         * 보유 주식 평가금은 평균단가가 아니라 현재가 기준으로 계산해야 한다.
         */
        BigDecimal stockValue = holdingResponses.stream()
                .map(com.stacknstock.backend.domain.game.dto.PortfolioStockResponse::evaluationAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAsset = runState.getCashBalance().add(stockValue);

        PortfolioResponse portfolio;

        portfolio = new PortfolioResponse(
                runId,
                dayNo,
                runState.getCashBalance(),
                totalAsset,
                holdingResponses,
                List.of() // 거래 로그는 Resume에서는 생략 (성능 고려)
        );

        Map<Long, Long> holdingQtyMap = holdings.stream()
                .collect(Collectors.toMap(
                        h -> h.getStock().getStockId(),
                        Holding::getQty,
                        Long::sum
                ));

        List<TradingStockResponse> stocks = latestPriceMap.values().stream()
                .map(latestPrice -> {
                    Long stockId = latestPrice.getStock().getStockId();
                    List<StockPrice> stockPrices = groupedPrices.getOrDefault(stockId, List.of());

                    List<PricePointResponse> priceHistory = stockPrices.stream()
                            .sorted(Comparator.comparing(StockPrice::getBaseDate))
                            .map(p -> new PricePointResponse(
                                    p.getBaseDate(),
                                    p.getClosePrice()
                            ))
                            .toList();

                    // 전일 가격 찾기 (baseDate 기준으로 바로 이전 값)
                    BigDecimal changeAmount = BigDecimal.ZERO;

                    if (!stockPrices.isEmpty()) {
                        List<StockPrice> sorted = stockPrices.stream()
                                .sorted(Comparator.comparing(StockPrice::getBaseDate))
                                .toList();

                        for (int i = 0; i < sorted.size(); i++) {
                            if (sorted.get(i).getBaseDate().equals(latestPrice.getBaseDate()) && i > 0) {
                                BigDecimal prevPrice = sorted.get(i - 1).getClosePrice();
                                changeAmount = latestPrice.getClosePrice().subtract(prevPrice);
                                break;
                            }
                        }
                    }

                    return new TradingStockResponse(
                            stockId,
                            latestPrice.getStock().getCompanyName(),
                            holdingQtyMap.getOrDefault(stockId, 0L),
                            latestPrice.getClosePrice(),
                            latestPrice.getReturnPct(),
                            changeAmount,
                            priceHistory
                    );
                })
                .toList();

        TradingScreenResponse tradingScreen = new TradingScreenResponse(
                runState.getCashBalance(),
                stockValue,
                totalAsset,
                stocks
        );

        /*
         * 이어하기에서는 상태 변경이 없으므로
         * daySummary / settlement / hasInspiration 은 조회용으로만 채운다.
         */
        boolean hasInspiration = runState.getInspirationCount() > 0;

        /**
         * 이어하기 시 랜덤 이벤트 조회 (상태 변경 없음)
         */
        Long randomEventId = scenarioDayRepository
                .findEventIdByRunIdAndDayNo(runId, dayNo)
                .orElse(null);

        /**
         * 이어하기 응답
         * - 상태 변경 없이 조회만 수행
         */
        return new DailyStartResponse(
                null, // DaySummary는 현재 스냅샷 구조 없음
                portfolio,
                articleArchive,
                tradingScreen,
                randomEventId,
                BigDecimal.ZERO, // 정산 금액은 DailyStartService에서만 처리
                hasInspiration
        );
    }
}