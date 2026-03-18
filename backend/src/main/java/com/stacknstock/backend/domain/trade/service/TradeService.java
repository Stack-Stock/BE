package com.stacknstock.backend.domain.trade.service;

import com.stacknstock.backend.domain.day.entity.Day;
import com.stacknstock.backend.domain.day.repository.DayRepository;
import com.stacknstock.backend.domain.game.entity.GameRun;
import com.stacknstock.backend.domain.game.entity.RunState;
import com.stacknstock.backend.domain.game.repository.RunStateRepository;
import com.stacknstock.backend.domain.trade.entity.Holding;
import com.stacknstock.backend.domain.trade.repository.HoldingRepository;
import com.stacknstock.backend.domain.stock.entity.Stock;
import com.stacknstock.backend.domain.stock.entity.StockPrice;
import com.stacknstock.backend.domain.stock.repository.StockPriceRepository;
import com.stacknstock.backend.domain.trade.dto.TradeOrderDto;
import com.stacknstock.backend.domain.trade.dto.TradeRequest;
import com.stacknstock.backend.domain.trade.entity.Trade;
import com.stacknstock.backend.domain.trade.enums.TradeSide;
import com.stacknstock.backend.domain.trade.repository.TradeRepository;
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
public class TradeService {

    private final RunStateRepository runStateRepository;
    private final DayRepository dayRepository;
    private final StockPriceRepository stockPriceRepository;
    private final HoldingRepository holdingRepository;
    private final TradeRepository tradeRepository;
    private final UserRepository userRepository;

    @Transactional
    public void executeTrade(Long userId, TradeRequest request) {

        /*
         * ===============================
         * 1. User / RunState 조회
         * ===============================
         */

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        RunState runState = runStateRepository
                .findByRunUserUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RUN_STATE_NOT_FOUND));

        GameRun run = runState.getRun();
        int currentDayNo = runState.getCurrentDayNo();

        /*
         * ===============================
         * 2. 현재 Day 조회
         * ===============================
         */

        Day currentDay = dayRepository
                .findByGameRunRunIdAndDayNo(run.getRunId(), currentDayNo)
                .orElseThrow(() -> new BusinessException(ErrorCode.DAY_NOT_FOUND));

        /*
         * ===============================
         * 3. 주문 종목 목록 추출
         * ===============================
         */

        List<Long> stockIds = request.orders()
                .stream()
                .map(TradeOrderDto::stockId)
                .distinct()
                .toList();

        /*
         * ===============================
         * 4. 현재 가격 조회 (N+1 방지)
         * ===============================
         */

        List<StockPrice> prices = stockPriceRepository
                .findByRunRunIdAndBaseDateAndStockStockIdIn(
                        run.getRunId(),
                        currentDayNo,
                        stockIds
                );

        Map<Long, BigDecimal> priceMap = prices.stream()
                .collect(Collectors.toMap(
                        p -> p.getStock().getStockId(),
                        StockPrice::getClosePrice,
                        (p1, p2) -> p2
                ));

        Map<Long, Stock> stockMap = prices.stream()
                .collect(Collectors.toMap(
                        p -> p.getStock().getStockId(),
                        StockPrice::getStock,
                        (s1, s2) -> s2
                ));

        /*
         * ===============================
         * 5. Holding 조회 (N+1 방지)
         * ===============================
         */

        Map<Long, Holding> holdingMap = holdingRepository
                .findByRunRunIdAndStockStockIdIn(run.getRunId(), stockIds)
                .stream()
                .collect(Collectors.toMap(
                        h -> h.getStock().getStockId(),
                        h -> h,
                        (h1, h2) -> h2
                ));

        /*
         * ===============================
         * 6. 주문 처리
         * ===============================
         */

        for (TradeOrderDto order : request.orders()) {

            Long stockId = order.stockId();
            TradeSide side = order.side();
            Long quantity = order.quantity();

            BigDecimal price = priceMap.get(stockId);

            if (price == null) {
                throw new BusinessException(ErrorCode.STOCK_PRICE_NOT_FOUND);
            }

            BigDecimal amount = price.multiply(BigDecimal.valueOf(quantity));

            Holding holding = holdingMap.get(stockId);

            /*
             * ===============================
             * BUY
             * ===============================
             */

            if (side == TradeSide.BUY) {

                if (runState.getCashBalance().compareTo(amount) < 0) {
                    throw new BusinessException(ErrorCode.ACTION_NOT_ALLOWED);
                }

                // 현금 차감
                runState.setCashBalance(
                        runState.getCashBalance().subtract(amount)
                );

                if (holding == null) {

                    Stock stock = stockMap.get(stockId);

                    holding = Holding.builder()
                            .run(run)
                            .stock(stock)
                            .qty(quantity)
                            .build();

                    holdingRepository.save(holding);

                    holdingMap.put(stockId, holding);

                } else {

                    Holding updatedHolding = Holding.builder()
                            .holdingId(holding.getHoldingId())
                            .run(holding.getRun())
                            .stock(holding.getStock())
                            .qty(holding.getQty() + quantity)
                            .build();

                    holdingRepository.save(updatedHolding);
                    holdingMap.put(stockId, updatedHolding);
                    holding = updatedHolding;
                }
            }

            /*
             * ===============================
             * SELL
             * ===============================
             */

            else {

                if (holding == null || holding.getQty() < quantity) {
                    throw new BusinessException(ErrorCode.ACTION_NOT_ALLOWED);
                }

                Long remainQty = holding.getQty() - quantity;

                if (remainQty == 0) {
                    holdingRepository.delete(holding);
                    holdingMap.remove(stockId);
                } else {

                    Holding updatedHolding = Holding.builder()
                            .holdingId(holding.getHoldingId())
                            .run(holding.getRun())
                            .stock(holding.getStock())
                            .qty(remainQty)
                            .build();

                    holdingRepository.save(updatedHolding);
                    holdingMap.put(stockId, updatedHolding);
                    holding = updatedHolding;
                }

            }

            /*
             * ===============================
             * Trade 로그 저장
             * ===============================
             */

            Trade trade = Trade.builder()
                    .run(run)
                    .day(currentDay)
                    .stock(stockMap.get(stockId))
                    .side(side)
                    .execQty(BigDecimal.valueOf(quantity))
                    .execPrice(price)
                    .execAmount(amount)
                    .settleDayNo(side == TradeSide.SELL ? currentDayNo + 3 : null)
                    .build();

            tradeRepository.save(trade);
        }

        /*
         * ===============================
         * 7. RunState 스냅샷 저장
         * ===============================
         */

        runStateRepository.save(runState);
    }
}