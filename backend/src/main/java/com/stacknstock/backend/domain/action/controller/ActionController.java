package com.stacknstock.backend.domain.action.controller;

import com.stacknstock.backend.domain.action.dto.ActionRequest;
import com.stacknstock.backend.domain.action.dto.ActionResultResponse;
import com.stacknstock.backend.domain.action.service.ActionService;
import com.stacknstock.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "행동", description = "게임 내 행동 실행 API")
@RestController
@RequestMapping("/api/actions")
@RequiredArgsConstructor
public class ActionController {

    private final ActionService actionService;

    /**
     * 게임 내 행동 실행 API
     *
     * 지원 행동:
     * - INFO_PHONE
     * - INFO_TV
     * - INFO_PAPER
     * - STUDY
     * - SLEEP
     */
    @PostMapping
    @Operation(
            summary = "행동 실행",
            description = "현재 로그인한 사용자가 특정 행동을 실행합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "행동 실행 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 행동 요청"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "진행 중인 게임 없음")
    })
    public ResponseEntity<ActionResultResponse> executeAction(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ActionRequest request
    ) {
        ActionResultResponse response = actionService.executeAction(
                userDetails.getUser().getUserId(),
                request
        );

        return ResponseEntity.ok(response);
    }
}