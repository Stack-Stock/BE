package com.stacknstock.backend.domain.game.controller;

import com.stacknstock.backend.domain.game.dto.DailyStartResponse;
import com.stacknstock.backend.domain.game.service.DailyStartService;
import com.stacknstock.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 하루 시작 API
 *
 * 게임에서 하루가 시작될 때 필요한 모든 데이터를 한 번에 반환한다.
 *
 * 반환 데이터
 * - 전날 요약 (추후 구현)
 * - 포트폴리오
 * - 기사 아카이브
 * - 거래 화면 데이터
 * - 랜덤 이벤트 여부
 * - T+3 정산 금액
 * - 번뜩임 지급 여부
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/runs")
@Tag(name = "인게임 하루 시작", description = "게임 하루 시작 API")
public class DailyStartController {

    private final DailyStartService dailyStartService;

    /**
     * 하루 시작 데이터 조회
     *
     * 프론트에서는 게임 하루가 시작될 때 이 API를 **한 번만 호출**한다.
     */
    @GetMapping("/{runId}/daily-start")
    @Operation(
            summary = "하루 시작 데이터 조회",
            description = """
                    게임 하루 시작 시 필요한 모든 데이터를 반환합니다.
                    
                    반환 데이터
                    - 전날 요약
                    - 포트폴리오
                    - 기사 아카이브
                    - 거래 화면 데이터
                    - 랜덤 이벤트 여부
                    - T+3 정산 금액
                    - 번뜩임 지급 여부
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "하루 시작 데이터 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            @ApiResponse(responseCode = "404", description = "게임 run을 찾을 수 없음")
    })
    public ResponseEntity<DailyStartResponse> getDailyStart(
            @AuthenticationPrincipal CustomUserDetails userDetails,

            @Parameter(
                    description = "게임 Run ID",
                    example = "1",
                    required = true
            )
            @PathVariable Long runId
    ) {

        DailyStartResponse response =
                dailyStartService.getDailyStart(userDetails.getUser().getUserId(), runId);

        return ResponseEntity.ok(response);
    }
}
