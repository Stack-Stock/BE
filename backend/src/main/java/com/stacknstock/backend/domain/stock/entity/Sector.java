package com.stacknstock.backend.domain.stock.entity;

import com.stacknstock.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sectors")
@Getter
@NoArgsConstructor
public class Sector extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sector_id")
    private Long sectorId;

    @Column(name = "name", nullable = false)
    private String name;
}
