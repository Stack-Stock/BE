package com.stacknstock.backend.domain.trade.repository;

import com.stacknstock.backend.domain.trade.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {

    /**
     * 오늘 정산되는 T+3 매도 금액 조회
     */
    @Query("""
    select coalesce(sum(t.execPrice * t.execQty),0)
    from Trade t
    where t.run.runId = :runId
    and t.side = com.stacknstock.backend.domain.trade.enums.TradeSide.SELL
    and t.settleDayNo = :dayNo
    """)
    BigDecimal getTodaySettlement(Long runId, Integer dayNo);

    /**
     * 오늘 정산되는 거래 내역 조회
     */
    @Query("""
    SELECT t
    FROM Trade t
    WHERE t.run.runId = :runId
    AND t.settleDayNo = :dayNo
    AND t.settledAt IS NULL
    AND t.side = 'SELL'
    """)
    List<Trade> findTodaySettlementTrades(
            Long runId,
            Integer dayNo
    );
}
