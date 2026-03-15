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
     * 게임 시작 이후 전체 주가 데이터
     *
     * 그래프 계산 및 거래 화면 표시용
     */
    @Query("""
    select sp
    from StockPrice sp
    join fetch sp.stock
    where sp.run.runId = :runId
""")
    List<StockPrice> findAllPrices(Long runId);
}
