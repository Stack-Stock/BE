package com.stacknstock.backend.domain.game.repository;

import com.stacknstock.backend.domain.game.entity.GameRun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GameRunRepository extends JpaRepository<GameRun, Long> {

    // 유저의 가장 최근 Run 찾기
    Optional<GameRun> findTopByUserUserIdOrderByRunIdDesc(Long userId);

}
