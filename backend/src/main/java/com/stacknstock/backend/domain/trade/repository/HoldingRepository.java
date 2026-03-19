package com.stacknstock.backend.domain.trade.repository;

import com.stacknstock.backend.domain.trade.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HoldingRepository extends JpaRepository<Holding, Long> {

    /**
     * 특정 Run의 보유 종목 + Stock 정보 조회
     *
     * - PortfolioResponse 구성
     * - TradingScreenResponse 구성
     *
     * fetch join을 사용하여 N+1 방지
     */
    @Query("""
        select h
        from Holding h
        join fetch h.stock
        where h.run.runId = :runId
    """)
    List<Holding> findHoldingsWithStock(Long runId);

    /**
     * 특정 Run에서 여러 종목의 Holding 조회
     */
    List<Holding> findByRunRunIdAndStockStockIdIn(Long runId, List<Long> stockIds);

}
