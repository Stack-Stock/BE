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

    /**
     * 현재 포트폴리오 조회
     *
     * Stock 정보를 fetch join 하여
     * 종목 정보 조회 시 추가 쿼리를 방지
     */
    @Query("""
    select h
    from Holding h
    join fetch h.stock
    where h.run.runId = :runId
""")
    List<Holding> findPortfolio(Long runId);
}
