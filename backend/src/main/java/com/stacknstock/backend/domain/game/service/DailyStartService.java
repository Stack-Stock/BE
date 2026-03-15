package com.stacknstock.backend.domain.game.service;

import java.util.Map;
import java.util.stream.Collectors;

import com.stacknstock.backend.domain.day.entity.DayState;
import com.stacknstock.backend.domain.day.repository.DayStateRepository;
import com.stacknstock.backend.domain.game.dto.ArticleJsonDto;
import com.stacknstock.backend.domain.trade.dto.PricePointResponse;
import com.stacknstock.backend.domain.game.dto.ArticleArchiveResponse;
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
import com.stacknstock.backend.domain.trade.dto.TradingScreenResponse;
import com.stacknstock.backend.domain.trade.dto.TradingStockResponse;
import com.stacknstock.backend.domain.trade.entity.Holding;
import com.stacknstock.backend.domain.trade.repository.HoldingRepository;
import com.stacknstock.backend.domain.trade.repository.TradeRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DailyStartService {

    private final RunStateRepository runStateRepository;
    private final ScenarioDayRepository scenarioDayRepository;
    private final HoldingRepository holdingRepository;
    private final StockPriceRepository stockPriceRepository;
    private final TradeRepository tradeRepository;
    private final DayStateRepository dayStateRepository;

    /** JSON 파싱을 위한 Jackson ObjectMapper */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 하루 시작 API
     *
     * 역할
     * 1. 현재 게임 상태 조회
     * 2. 번뜩임 지급 여부 계산
     * 3. 기사 아카이브 조회
     * 4. 포트폴리오 계산
     * 5. 거래 화면 데이터 생성
     * 6. T+3 정산 금액 조회
     */
    @Transactional
    public DailyStartResponse getDailyStart(Long runId) {

        /**
         * 현재 게임 상태 조회
         */
        RunState runState = runStateRepository
                .findByRunRunId(runId)
                .orElseThrow();

        Integer dayNo = runState.getCurrentDayNo();

        /**
         * 번뜩임 지급 로직
         * 공부 3회마다 번뜩임 지급
         */
        DayState today = dayStateRepository
                .findByDayGameRunRunIdAndDayDayNo(runId, dayNo)
                .orElseThrow();

        DayState yesterday = dayStateRepository
                .findByDayGameRunRunIdAndDayDayNo(runId, dayNo - 1)
                .orElse(null);

        boolean hasInspiration = false;

        if (yesterday != null
                && yesterday.getStudyDone()
                && !today.getStudyDone()
                && runState.getTotalStudyCnt() % 3 == 0) {

            runState.setInspirationCount(
                    runState.getInspirationCount() + 1
            );

            hasInspiration = true;
        }

        /**
         * 기사 아카이브 조회
         * ScenarioDay → GameCase → Stock
         * Repository에서 fetch join으로 N+1 제거
         */
        List<ScenarioDay> articles =
                scenarioDayRepository.findArticleArchive(runId, dayNo);

        /**
         * Entity → DTO 변환
         *
         * article_json 컬럼에는 다음과 같은 JSON 배열이 들어있다.
         * [
         *   {
         *     "article_id": "...",
         *     "title": "...",
         *     "url": "...",
         *     "published_at": "2025-03-24 17:51:09"
         *   }
         * ]
         *
         * ArticleJsonDto로 파싱하여 publishedAt과 title 등을 추출한다.
         */
        List<ArticleResponse> articleResponses = articles.stream()
                .map(sd -> {

                    GameCase gameCase = sd.getGameCase();

                    // article_json(JSONB) → ArticleJsonDto 리스트로 파싱
                    ArticleJsonDto article;
                    try {

                        List<ArticleJsonDto> articleList =
                                objectMapper.readValue(
                                        gameCase.getArticleJson(),
                                        new TypeReference<List<ArticleJsonDto>>() {}
                                );

                        // 현재 구조상 기사 하나만 사용
                        article = articleList.get(0);

                    } catch (Exception e) {
                        throw new RuntimeException("article_json 파싱 실패", e);
                    }

                   return new ArticleResponse(
                            gameCase.getCaseId(),
                            article.title(),
                            article.publishedAt(),
                            article.url(),
                            gameCase.getStory(),
                            sd.getDayNo(),
                            gameCase.getStock().getStockId()
                    );

                })
                .toList();

        ArticleArchiveResponse articleArchive =
                new ArticleArchiveResponse(articleResponses);

        /**
         * 포트폴리오 계산
         */
        List<Holding> holdings =
                holdingRepository.findPortfolio(runId);

        BigDecimal stockValue = holdings.stream()
                .map(h -> h.getAvgCost()
                        .multiply(BigDecimal.valueOf(h.getQty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAsset =
                runState.getCashBalance().add(stockValue);

        PortfolioResponse portfolio =
                new PortfolioResponse(
                        runId,
                        dayNo,
                        runState.getCashBalance(),
                        totalAsset,
                        List.of(),   // 보유 종목 목록 (추후 확장)
                        List.of()    // 거래 로그 (추후 확장)
                );

        /**
         * 거래 화면 데이터
         *
         * prices에는 게임 시작 이후 모든 날짜의 가격 데이터가 들어있다.
         * 이를 종목별로 그룹핑하여 price history를 생성한다.
         */
        List<StockPrice> prices =
                stockPriceRepository.findAllPrices(runId);

        // 종목별 price history 생성
        Map<Long, List<PricePointResponse>> priceHistoryMap =
                prices.stream()
                        .collect(Collectors.groupingBy(
                                p -> p.getStock().getStockId(),
                                Collectors.mapping(
                                        p -> new PricePointResponse(
                                                p.getBaseDate(),
                                                p.getClosePrice()
                                        ),
                                        Collectors.toList()
                                )
                        ));

        // 거래 화면 종목 리스트 생성
        List<TradingStockResponse> stocks =
                prices.stream()
                        .map(p -> {

                            Long stockId = p.getStock().getStockId();

                            return new TradingStockResponse(
                                    stockId,
                                    p.getStock().getCompanyName(),
                                    0L,
                                    p.getClosePrice(),
                                    p.getReturnPct(),
                                    BigDecimal.ZERO,
                                    priceHistoryMap.getOrDefault(stockId, List.of())
                            );

                        })
                        .distinct()
                        .toList();

        TradingScreenResponse tradingScreen =
                new TradingScreenResponse(
                        runState.getCashBalance(),
                        stockValue,
                        totalAsset,
                        stocks
                );

        /**
         * T+3 정산 금액
         */
        BigDecimal settlement =
                tradeRepository.getTodaySettlement(runId, dayNo);

        /**
         * 최종 응답 반환
         */
        return new DailyStartResponse(
                null,           // DaySummary (추후 추가)
                portfolio,
                articleArchive,
                tradingScreen,
                false,          // RandomEvent 여부 (추후 구현)
                settlement,
                hasInspiration
        );
    }
}
