package com.stacknstock.backend.domain.game.controller;

import com.stacknstock.backend.domain.game.dto.ContinueRunResponse;
import com.stacknstock.backend.domain.game.dto.StartGameResponse;
import com.stacknstock.backend.domain.game.service.GameRunService;
import com.stacknstock.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "게임 시작", description = "새 게임 시작 및 이어하기 API")
@RestController
@RequestMapping("/api/runs")
@RequiredArgsConstructor
public class GameRunController {

    private final GameRunService gameRunService;

    @PostMapping("/new")
    @Operation(summary = "새 게임 시작", description = "현재 로그인한 사용자의 새 게임을 시작합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "새 게임 시작 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<StartGameResponse> startGame(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        StartGameResponse response = gameRunService.startGame(
                userDetails.getUser().getUserId()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/continue")
    @Operation(summary = "이어하기", description = "현재 진행 중인 게임을 이어서 시작합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게임 이어하기 성공"),
            @ApiResponse(responseCode = "404", description = "진행 중인 게임 없음")
    })
    public ResponseEntity<ContinueRunResponse> continueRun(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        ContinueRunResponse response =
                gameRunService.continueRun(userDetails.getUser().getUserId());

        return ResponseEntity.ok(response);
    }
}