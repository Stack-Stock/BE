package com.stacknstock.backend.domain.event.repository;

import com.stacknstock.backend.domain.event.entity.RandomEventRun;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RandomEventRunRepository extends JpaRepository<RandomEventRun, Long> {
}
