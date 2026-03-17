package com.stacknstock.backend.domain.game.repository;

import com.stacknstock.backend.domain.game.entity.GameRun;
import com.stacknstock.backend.domain.game.enums.RunStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameRunRepository extends JpaRepository<GameRun, Long> {

    // 유저의 가장 최근 Run 찾기
    Optional<GameRun> findTopByUserUserIdOrderByRunIdDesc(Long userId);

    // 현재 진행 중인 게임 가져오기
    Optional<GameRun> findByUserUserIdAndStatus(Long userId, RunStatus status);

    // 유저의 모든 Run 정보 찾기
    List<GameRun> findAllByUserUserIdAndStatus(Long userId, RunStatus status);

}
