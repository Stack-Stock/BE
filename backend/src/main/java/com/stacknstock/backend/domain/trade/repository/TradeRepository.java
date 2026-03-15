package com.stacknstock.backend.domain.trade.repository;

import com.stacknstock.backend.domain.trade.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

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

}
