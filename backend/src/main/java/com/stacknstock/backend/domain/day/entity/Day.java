package com.stacknstock.backend.domain.day.entity;

import com.stacknstock.backend.domain.game.entity.GameRun;
import com.stacknstock.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "days")
public class Day extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dayId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id")
    private GameRun gameRun;

    private Integer dayNo;

    private Integer ap_base;

    private Integer ap_used;

    private Boolean inspiration_used;

    private Boolean is_sleep_done;
}
