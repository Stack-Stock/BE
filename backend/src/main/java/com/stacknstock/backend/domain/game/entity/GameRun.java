package com.stacknstock.backend.domain.game.entity;

import com.stacknstock.backend.domain.game.enums.RunStatus;
import com.stacknstock.backend.domain.user.entity.User;
import com.stacknstock.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_runs")
@Getter
@NoArgsConstructor
public class GameRun extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "run_id")
    private Long runId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RunStatus status;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Column(name = "start_cash", nullable = false)
    private BigDecimal startCash;

    // GameRun 새로 생성하기
    public static GameRun create(User user, RunStatus status, java.math.BigDecimal startCash) {
        GameRun gameRun = new GameRun();
        gameRun.user = user;
        gameRun.status = status;
        gameRun.startCash = startCash;
        gameRun.startAt = java.time.LocalDateTime.now();
        return gameRun;
    }
}
