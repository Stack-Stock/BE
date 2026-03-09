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
    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "event_name", nullable = false)
    private String eventName;

    @Column(name = "event_content", columnDefinition = "jsonb")
    private String eventContent;

    @Column(name = "event_probability", nullable = false)
    private BigDecimal eventProbability;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;
}
