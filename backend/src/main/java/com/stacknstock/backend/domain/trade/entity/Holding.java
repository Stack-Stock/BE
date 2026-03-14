package com.stacknstock.backend.domain.trade.entity;

import com.stacknstock.backend.domain.game.entity.GameRun;
import com.stacknstock.backend.domain.stock.entity.Stock;
import com.stacknstock.backend.global.entity.SnapshotEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "holdings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_holding_run_stock",
                        columnNames = {"run_id", "stock_id"}
                )
        }
)
@Getter
@NoArgsConstructor
public class Holding extends SnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "holding_id")
    private Long holdingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    private GameRun run;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(name = "qty", nullable = false)
    private Long qty;

    @Column(name = "avg_cost", nullable = false)
    private BigDecimal avgCost;
}
