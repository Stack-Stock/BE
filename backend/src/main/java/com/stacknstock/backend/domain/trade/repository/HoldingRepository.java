package com.stacknstock.backend.domain.trade.repository;

import com.stacknstock.backend.domain.trade.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoldingRepository extends JpaRepository<Holding, Long> {
}
