package com.stacknstock.backend.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.EntityListeners;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class SnapshotEntity {

    @LastModifiedDate
    @Column(name = "updated_at")
    protected LocalDateTime updatedAt;
}
