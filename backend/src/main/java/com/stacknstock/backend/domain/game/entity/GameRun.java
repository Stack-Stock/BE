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
    private Long runId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private RunStatus status;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private BigDecimal startCash;
}
