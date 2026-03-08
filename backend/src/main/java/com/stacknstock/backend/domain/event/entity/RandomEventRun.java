package com.stacknstock.backend.domain.event.entity;

import com.stacknstock.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "random_event_run")
@Getter
@NoArgsConstructor
public class RandomEventRun extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rerId;

    private Long runId;

    private Long dayId;

    private Long eventId;

    @Column(columnDefinition = "jsonb")
    private String resultJson;

    private BigDecimal cashDelta;
}
