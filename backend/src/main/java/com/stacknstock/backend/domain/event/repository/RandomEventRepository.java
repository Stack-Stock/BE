package com.stacknstock.backend.domain.event.repository;

import com.stacknstock.backend.domain.event.entity.RandomEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RandomEventRepository extends JpaRepository<RandomEvent, Long> {
}
