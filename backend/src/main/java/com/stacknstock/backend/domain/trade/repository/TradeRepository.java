package com.stacknstock.backend.domain.trade.repository;

import com.stacknstock.backend.domain.trade.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository extends JpaRepository<Trade, Long> {
}
