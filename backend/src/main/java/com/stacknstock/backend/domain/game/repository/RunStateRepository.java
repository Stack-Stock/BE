package com.stacknstock.backend.domain.game.repository;

import com.stacknstock.backend.domain.game.entity.RunState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RunStateRepository extends JpaRepository<RunState, Long> {

    // RunId로 게임런 찾기
    Optional<RunState> findByRunRunId(Long runId);

    /**
     * 유저 기준 현재 RunState 조회
     *
     * RunState
     *  -> run
     *      -> user
     *          -> userId
     */
    Optional<RunState> findByRunUserUserId(Long userId);

    // RunStateRepository.java 내부에 추가
    Optional<RunState> findFirstByRunUserUserIdOrderByRunRunIdDesc(Long userId);
}
