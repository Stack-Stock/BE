package com.stacknstock.backend.domain.event.entity;

import com.stacknstock.backend.domain.day.entity.Day;
import com.stacknstock.backend.domain.game.entity.GameRun;
import com.stacknstock.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "random_event_run")
@Getter
@NoArgsConstructor
public class RandomEventRun extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rer_id")
    private Long rerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    private GameRun run;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_id", nullable = false)
    private Day day;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private RandomEvent event;

    @Column(name = "result_json", columnDefinition = "jsonb")
    private String resultJson;

    @Column(name = "cash_delta")
    private BigDecimal cashDelta;
}
