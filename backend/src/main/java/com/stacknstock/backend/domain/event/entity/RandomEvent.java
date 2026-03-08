package com.stacknstock.backend.domain.event.entity;

import com.stacknstock.backend.domain.event.enums.EventType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "random_event")
@Getter
@NoArgsConstructor
public class RandomEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventId;

    private String eventName;

    @Column(columnDefinition = "jsonb")
    private String eventContent;

    private BigDecimal eventProbability;

    @Enumerated(EnumType.STRING)
    private EventType eventType;
}
