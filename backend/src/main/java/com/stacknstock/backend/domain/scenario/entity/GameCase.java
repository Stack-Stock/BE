package com.stacknstock.backend.domain.scenario.entity;

import com.stacknstock.backend.domain.stock.entity.Stock;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "game_case")
public class GameCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "case_id")
    private Long caseId;

    @Column(nullable = false, length = 200)
    private String story;

    @Lob
    @Column(nullable = false)
    private String phone;

    @Lob
    @Column(nullable = false)
    private String tv;

    @Lob
    @Column(nullable = false)
    private String newspaper;

    @Column(name = "article_json", columnDefinition = "json")
    private String articleJson;

    @Lob
    @Column(nullable = false)
    private String reason;

    @Column(name = "up_down_json", columnDefinition = "json", nullable = false)
    private String upDownJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;
}