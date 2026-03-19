package com.stacknstock.backend.domain.day.entity;

import com.stacknstock.backend.global.entity.SnapshotEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "day_state")
public class DayState extends SnapshotEntity implements Persistable<Long> {

    @Id
    @Column(name = "day_id")
    private Long dayId;

    // 💡 [핵심] JPA가 DB에 저장하지 않는 임시 플래그
    @Transient
    @Builder.Default
    private boolean isNewRecord = true;

    // 💡 Persistable 구현: 내 ID가 무엇인지 JPA에게 알려줌
    @Override
    public Long getId() {
        return this.dayId;
    }

    // 💡 Persistable 구현: "나 무조건 새 데이터야! INSERT 해!"
    @Override
    public boolean isNew() {
        return this.isNewRecord;
    }

    // 💡 INSERT가 되거나 DB에서 불러와졌을 때는 기존 데이터 취급(false)
    @PostPersist
    @PostLoad
    protected void load() {
        this.isNewRecord = false;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "day_id")
    private Day day;

    @Column(name = "ap_remaining", nullable = false)
    private Integer apRemaining;

    @Column(name = "study_done", nullable = false)
    private Boolean studyDone;

    // 💡 [수정] PostgreSQL의 jsonb 타입과 호환되도록 어노테이션 추가
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "today_summary", columnDefinition = "jsonb")
    private String todaySummary;

    // 새로운 일차 상태 스냅샷 생성
    public static DayState create(Day day, Integer apRemaining, Boolean studyDone) {
        DayState dayState = new DayState();
        dayState.day = day;
        // 💡 JPA가 @MapsId로 알아서 넣어주므로 직접 할당 제거
        dayState.apRemaining = apRemaining;
        dayState.studyDone = studyDone;
        dayState.todaySummary = null;
        return dayState;
    }
}