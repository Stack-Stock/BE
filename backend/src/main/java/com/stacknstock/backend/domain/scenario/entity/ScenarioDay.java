package com.stacknstock.backend.domain.scenario.entity;

import com.stacknstock.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "scenario_days")
@Getter
@NoArgsConstructor
public class ScenarioDay extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scenario_id")
    private Long scenarioId;

    @Column(name = "run_id", nullable = false)
    private Long runId;

    @Column(name = "day_no", nullable = false)
    private Integer dayNo;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "event_id")
    private Long eventId;
}
