package com.stacknstock.backend.domain.game.entity;

import com.stacknstock.backend.global.entity.SnapshotEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;

@Entity
@Table(name = "run_state")
@Getter
@Setter
@NoArgsConstructor
public class RunState extends SnapshotEntity implements Persistable<Long> {

    @Id
    @Column(name = "run_id")
    private Long runId;

    // 💡 [핵심] JPA가 DB에 저장하지 않는 임시 플래그
    @Transient
    private boolean isNewRecord = true;

    // 💡 Persistable 구현
    @Override
    public Long getId() {
        return this.runId;
    }

    // 💡 Persistable 구현: 무조건 INSERT 유도
    @Override
    public boolean isNew() {
        return this.isNewRecord;
    }

    @PostPersist
    @PostLoad
    protected void load() {
        this.isNewRecord = false;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "run_id", nullable = false)
    private GameRun run;

    @Column(name = "current_day_no", nullable = false)
    private Integer currentDayNo;

    @Column(name = "cash_balance", nullable = false, columnDefinition = "numeric")
    private BigDecimal cashBalance;

    @Column(name = "inspiration_count", nullable = false)
    private Integer inspirationCount;

    @Column(name = "last_action_id")
    private Long lastActionId;

    @Column(name = "total_study_cnt", nullable = false)
    private Integer totalStudyCnt;

    // 새로운 RunState 스냅샷 생성
    public static RunState create(GameRun run, Integer currentDayNo, java.math.BigDecimal cashBalance, Integer inspirationCount, Integer totalStudyCnt) {
        RunState runState = new RunState();
        runState.run = run;
        // 💡 JPA가 @MapsId로 알아서 넣어주므로 직접 할당 제거
        runState.currentDayNo = currentDayNo;
        runState.cashBalance = cashBalance;
        runState.inspirationCount = inspirationCount;
        runState.lastActionId = null;
        runState.totalStudyCnt = totalStudyCnt;
        return runState;
    }
}