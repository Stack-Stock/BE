package com.stacknstock.backend.domain.scenario.repository;

import com.stacknstock.backend.domain.scenario.entity.ScenarioDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    /**
     * 기사 아카이브 조회
     *
     * ScenarioDay
     *  -> GameCase
     *  -> Stock
     *
     * fetch join으로 N+1 제거
     */
    @Query("""
        select sd
        from ScenarioDay sd
        join fetch sd.gameCase gc
        join fetch gc.stock s
        where sd.run.runId = :runId
        and sd.dayNo <= :dayNo
        order by sd.dayNo asc
    """)
    List<ScenarioDay> findArticleArchive(
            @Param("runId") Long runId,
            @Param("dayNo") Integer dayNo
    );

    Optional<ScenarioDay> findByRunRunIdAndDayNo(Long runId, Integer dayNo);


    /**
     * 특정 run의 특정 날짜의 랜덤이벤트 아이디 받아오기
     * @param runId
     * @param dayNo
     * @return
     */
    @Query("""
        SELECT sd.event.eventId
        FROM ScenarioDay sd
        WHERE sd.run.runId = :runId
          AND sd.dayNo = :dayNo
    """)
    Optional<Long> findEventIdByRunIdAndDayNo(Long runId, Integer dayNo);

    /**
     * 특정 GameRun의 전체 시나리오를 day 순서대로 조회
     */
    List<ScenarioDay> findByRunRunIdOrderByDayNoAsc(Long runId);

}
