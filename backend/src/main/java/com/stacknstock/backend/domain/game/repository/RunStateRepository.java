package com.stacknstock.backend.domain.game.repository;

import com.stacknstock.backend.domain.game.entity.RunState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RunStateRepository extends JpaRepository<RunState, Long> {
}
