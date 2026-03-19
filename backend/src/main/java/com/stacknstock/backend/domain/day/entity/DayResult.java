package com.stacknstock.backend.domain.day.entity;

import com.stacknstock.backend.domain.game.entity.GameRun;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "day_result",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"run_id", "day_no"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DayResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dayResultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    private GameRun run;

    @Column(name = "day_no", nullable = false)
    private Integer dayNo;

    @Column(name = "cash_balance", nullable = false)
    private BigDecimal cashBalance;

    @Column(name = "stock_value", nullable = false)
    private BigDecimal stockValue;

    @Column(name = "total_asset", nullable = false)
    private BigDecimal totalAsset;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}