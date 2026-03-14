package com.stacknstock.backend.domain.stock.repository;

import com.stacknstock.backend.domain.stock.entity.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {

    // 특정 게임(run), 특정 날짜(day), 여러 종목(stockIds)에 대한 주가 정보를 한 번에 조회
    List<StockPrice> findByRunRunIdAndBaseDateAndStockStockIdIn(
            Long runId,
            Integer baseDate,
            List<Long> stockIds
    );
}
