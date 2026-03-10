package com.stacknstock.backend.domain.stock.repository;

import com.stacknstock.backend.domain.stock.entity.StockPrice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockPriceRepository extends JpaRepository<StockPrice, Long> {
}
