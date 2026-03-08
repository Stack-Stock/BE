package com.stacknstock.backend.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public abstract class SnapshotEntity {

    @Column(name = "updated_at")
    protected LocalDateTime updatedAt;
}
