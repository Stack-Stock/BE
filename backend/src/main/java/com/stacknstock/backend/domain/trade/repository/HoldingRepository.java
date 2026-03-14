package com.stacknstock.backend.domain.trade.repository;

import com.stacknstock.backend.domain.trade.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HoldingRepository extends JpaRepository<Holding, Long> {

    // 특정 게임런의 전체 보유 종목 조회
    @Query("""
    SELECT h
    FROM Holding h
    JOIN FETCH h.stock
    WHERE h.run.runId = :runId
    """)
    List<Holding> findByRunRunId(Long runId);
}
