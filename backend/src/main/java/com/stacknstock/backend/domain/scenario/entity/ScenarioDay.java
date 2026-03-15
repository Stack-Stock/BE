package com.stacknstock.backend.domain.scenario.entity;

import com.stacknstock.backend.domain.event.entity.RandomEvent;
import com.stacknstock.backend.domain.event.entity.RandomEventRun;
import com.stacknstock.backend.domain.game.entity.GameRun;
import com.stacknstock.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;

@Entity
@Table(name = "scenario_days")
@Getter
@NoArgsConstructor
public class ScenarioDay extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scenario_id")
    private Long scenarioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id")
    private GameRun run;

    @Column(name = "day_no", nullable = false)
    private Integer dayNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id")
    private GameCase gameCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private RandomEvent event;
}
