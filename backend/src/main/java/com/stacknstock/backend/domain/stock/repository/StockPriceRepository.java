package com.stacknstock.backend.domain.stock.repository;

import com.stacknstock.backend.domain.stock.entity.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {

    // 특정 게임런, 특정 종목, 특정 일차의 가격 정보 조회
    Optional<StockPrice> findByRunRunIdAndStockStockIdAndBaseDate(
            Long runId,
            Long stockId,
            Integer baseDate
    );
}
