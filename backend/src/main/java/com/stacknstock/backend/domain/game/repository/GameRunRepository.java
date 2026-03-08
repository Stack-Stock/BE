package com.stacknstock.backend.domain.game.repository;

import com.stacknstock.backend.domain.game.entity.GameRun;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRunRepository extends JpaRepository<GameRun, Long> {
}
