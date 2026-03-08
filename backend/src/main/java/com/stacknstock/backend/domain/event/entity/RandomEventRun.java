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
    private Long rerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id")
    private GameRun run;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_id")
    private Day day;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private RandomEvent event;

    @Column(columnDefinition = "jsonb")
    private String resultJson;

    private BigDecimal cashDelta;
}
