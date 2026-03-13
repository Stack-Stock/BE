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
    @Column(name = "day_id")
    private Long dayId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    private GameRun gameRun;

    @Column(name = "day_no", nullable = false)
    private Integer dayNo;

    @Column(name = "ap_base", nullable = false)
    private Integer apBase;

    @Column(name = "ap_used", nullable = false)
    private Integer apUsed;

    @Column(name = "inspiration_used", nullable = false)
    private Boolean inspirationUsed;

    @Column(name = "is_sleep_done", nullable = false)
    private Boolean isSleepDone;

    // N일차 기록 생성
    public static Day create(GameRun gameRun, Integer dayNo, Integer apBase) {
        Day day = new Day();
        day.gameRun = gameRun;
        day.dayNo = dayNo;
        day.apBase = apBase;
        day.apUsed = 0;
        day.inspirationUsed = false;
        day.isSleepDone = false;
        return day;
    }
}
