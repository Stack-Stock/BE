package com.stacknstock.backend.domain.trade.entity;

import com.stacknstock.backend.domain.game.entity.GameRun;
import com.stacknstock.backend.domain.stock.entity.Stock;
import com.stacknstock.backend.global.entity.SnapshotEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "holdings")
@Getter
@NoArgsConstructor
public class Holding extends SnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long holdingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id")
    private GameRun run;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;

    private BigDecimal qty;

    private BigDecimal avgCost;
}
