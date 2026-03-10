package com.stacknstock.backend.domain.scenario.repository;

import com.stacknstock.backend.domain.scenario.entity.GameCase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameCaseRepository extends JpaRepository<GameCase, Long> {
}
