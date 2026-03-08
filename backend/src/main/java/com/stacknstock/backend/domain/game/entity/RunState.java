package com.stacknstock.backend.domain.game.entity;

import com.stacknstock.backend.global.entity.SnapshotEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "run_state")
@Getter
@NoArgsConstructor
public class RunState extends SnapshotEntity {

    @Id
    private Long runId;

    private Integer currentDayNo;

    private BigDecimal cashBalance;

    private Integer inspirationCount;

    private Long lastActionId;
}
