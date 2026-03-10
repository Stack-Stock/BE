package com.stacknstock.backend.domain.day.repository;

import com.stacknstock.backend.domain.day.entity.Day;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DayRepository extends JpaRepository<Day, Long> {
}
