package com.stacknstock.backend.domain.trade.entity;

import com.stacknstock.backend.domain.day.entity.Day;
import com.stacknstock.backend.domain.game.entity.GameRun;
import com.stacknstock.backend.domain.stock.entity.Stock;
import com.stacknstock.backend.domain.trade.enums.TradeSide;
import com.stacknstock.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "trades",
        indexes = {
                @Index(name = "idx_trade_run", columnList = "run_id"),
                @Index(name = "idx_trade_run_day", columnList = "run_id, day_id")
        }
)
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class Trade extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trade_id")
    private Long tradeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    private GameRun run;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_id", nullable = false)
    private Day day;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false)
    private TradeSide side;

    @Column(name = "exec_qty", nullable = false)
    private BigDecimal execQty;

    @Column(name = "exec_price", nullable = false)
    private BigDecimal execPrice;

    @Column(name = "exec_amount", nullable = false)
    private BigDecimal execAmount;

    @Column(name = "settle_day_no")
    private Integer settleDayNo;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;
}
