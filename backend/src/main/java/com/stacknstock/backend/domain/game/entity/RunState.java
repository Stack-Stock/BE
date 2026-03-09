package com.stacknstock.backend.domain.game.entity;

import com.stacknstock.backend.global.entity.SnapshotEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "run_state")
@Getter
@NoArgsConstructor
public class RunState extends SnapshotEntity {

    @Id
    @Column(name = "run_id")
    private Long runId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "run_id", nullable = false)
    private GameRun run;

    @Column(name = "current_day_no", nullable = false)
    private Integer currentDayNo;

    @Column(name = "cash_balance", nullable = false)
    private BigDecimal cashBalance;

    @Column(name = "inspiration_count", nullable = false)
    private Integer inspirationCount;

    @Column(name = "last_action_id")
    private Long lastActionId;
}
