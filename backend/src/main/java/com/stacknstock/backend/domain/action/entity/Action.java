package com.stacknstock.backend.domain.action.entity;

import com.stacknstock.backend.domain.action.enums.ActionType;
import com.stacknstock.backend.domain.day.entity.Day;
import com.stacknstock.backend.domain.game.entity.GameRun;
import com.stacknstock.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "actions")
public class Action extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "action_id")
    private Long actionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_id", nullable = false)
    private Day day;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    private GameRun run;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private ActionType actionType;

    @Column(name = "ap_cost", nullable = false)
    private Integer apCost;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meta", columnDefinition = "jsonb")
    private String meta;

    @Builder
    public Action(
            Day day,
            GameRun run,
            ActionType actionType,
            Integer apCost,
            String meta
    ) {
        this.day = day;
        this.run = run;
        this.actionType = actionType;
        this.apCost = apCost;
        this.meta = meta;
    }
}
