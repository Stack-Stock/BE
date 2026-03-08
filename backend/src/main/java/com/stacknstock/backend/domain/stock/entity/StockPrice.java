package com.stacknstock.backend.domain.stock.entity;

import com.stacknstock.backend.domain.game.entity.GameRun;
import com.stacknstock.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "stock_prices")
public class StockPrice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "price_id")
    private Long priceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "run_id", nullable = false)
    private GameRun run;

    @Column(name = "base_date", nullable = false)
    private Integer baseDate;

    @Column(name = "close_price", columnDefinition = "jsonb", nullable = false)
    private String closePrice;

    @Column(name = "return_pct", columnDefinition = "jsonb", nullable = false)
    private String returnPct;
}