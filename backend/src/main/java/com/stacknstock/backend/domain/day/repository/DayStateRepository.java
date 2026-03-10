package com.stacknstock.backend.domain.day.repository;

import com.stacknstock.backend.domain.day.entity.DayState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DayStateRepository extends JpaRepository<DayState, Long> {
}
