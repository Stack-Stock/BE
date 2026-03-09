package com.stacknstock.backend.domain.stock.entity;

import com.stacknstock.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stocks")
@Getter
@NoArgsConstructor
public class Stock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_id")
    private Long stockId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id", nullable = false)
    private Sector sector;

    @Column(name = "ticker", nullable = false, unique = true)
    private String ticker;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "start_price", nullable = false)
    private Long startPrice;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
