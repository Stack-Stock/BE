package com.stacknstock.backend.domain.day.entity;

import com.stacknstock.backend.global.entity.SnapshotEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "day_state")
public class DayState extends SnapshotEntity {

    @Id
    @Column(name = "day_id")
    private Long dayId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "day_id")
    private Day day;

    @Column(name = "ap_remaining", nullable = false)
    private Integer apRemaining;

    @Column(name = "study_count", nullable = false)
    private Integer studyCount;

    @Column(name = "today_summary", columnDefinition = "jsonb")
    private String todaySummary;

    // 새로운 일차 상태 스냅샷 생성
    public static DayState create(Day day, Integer apRemaining, Integer studyCount) {
        DayState dayState = new DayState();
        dayState.day = day;
        dayState.dayId = day.getDayId();
        dayState.apRemaining = apRemaining;
        dayState.studyCount = studyCount;
        dayState.todaySummary = null;
        return dayState;
    }
}
