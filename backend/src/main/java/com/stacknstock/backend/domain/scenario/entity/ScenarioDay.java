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
    private Long scenarioId;

    private Long runId;

    private Integer dayNo;

    private Long caseId;

    private Long eventId;
}
