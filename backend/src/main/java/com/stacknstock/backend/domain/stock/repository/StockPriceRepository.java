package com.stacknstock.backend.domain.stock.repository;

import com.stacknstock.backend.domain.stock.entity.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {

    // 특정 게임(run), 특정 날짜(day), 여러 종목(stockIds)에 대한 주가 정보를 한 번에 조회
    List<StockPrice> findByRunRunIdAndBaseDateAndStockStockIdIn(
            Long runId,
            Integer baseDate,
            List<Long> stockIds
    );

    /**
     * 특정 run + day 기준 최신 주가 조회
     *
     * - 현재가
     * - 전일 대비 등락률 계산
     */
    @Query("""
        select sp
        from StockPrice sp
        join fetch sp.stock
        where sp.run.runId = :runId
          and sp.baseDate = :dayNo
    """)
    List<StockPrice> findLatestPrices(Long runId, Integer dayNo);

    /**
     * 게임 시작 이후 '현재 일차(dayNo)까지의' 전체 주가 히스토리 조회
     */
    @Query("""
        select sp
        from StockPrice sp
        join fetch sp.stock
        where sp.run.runId = :runId
          and sp.baseDate <= :dayNo 
        order by sp.baseDate asc
    """)
    List<StockPrice> findPriceHistory(Long runId, Integer dayNo); // 파라미터 추가!
}
