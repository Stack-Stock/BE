package com.stacknstock.backend.domain.scenario.repository;

import com.stacknstock.backend.domain.scenario.entity.ScenarioDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ScenarioDayRepository extends JpaRepository<ScenarioDay, Long> {
    @Query("""
        select sd
        from ScenarioDay sd
        join fetch sd.gameCase
        where sd.run.runId = :runId
        and sd.dayNo = :dayNo
        """)
    Optional<ScenarioDay> findWithGameCase(@Param("runId") Long runId, @Param("dayNo") Integer dayNo);
}
