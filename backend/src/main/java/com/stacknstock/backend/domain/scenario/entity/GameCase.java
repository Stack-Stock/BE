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

    @Column(name = "story", nullable = false, length = 200)
    private String story;

    @Lob
    @Column(name = "phone", nullable = false)
    private String phone;

    @Lob
    @Column(name = "tv", nullable = false)
    private String tv;

    @Lob
    @Column(name = "newspaper", nullable = false)
    private String newspaper;

    @Column(name = "article_json", columnDefinition = "jsonb")
    private String articleJson;

    @Lob
    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "up_down_json", columnDefinition = "jsonb", nullable = false)
    private String upDownJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock; // 관련 종목 ID
}