package com.stacknstock.backend.domain.user.service;

import com.stacknstock.backend.domain.game.entity.GameRun;
import com.stacknstock.backend.domain.game.enums.RunStatus;
import com.stacknstock.backend.domain.game.repository.GameRunRepository;
import com.stacknstock.backend.domain.user.dto.UserResponse;
import com.stacknstock.backend.domain.user.entity.User;
import com.stacknstock.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final GameRunRepository gameRunRepository;

    /**
     * 내 정보 조회 + 이어하기 여부
     */
    public UserResponse getMyInfo(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));

        /**
         * 이어하기 여부 판단
         *
         * 조건:
         * - 해당 유저의 진행 중인 GameRun 존재 여부
         */
        GameRun run =
                gameRunRepository.findByUserUserIdAndStatus(userId, RunStatus.RUNNING)
                        .orElse(null);

        boolean canContinue = (run != null);

        return new UserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                canContinue
        );
    }
}
