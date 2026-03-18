package com.stacknstock.backend.domain.game.controller;

import com.stacknstock.backend.domain.game.dto.DailyStartResponse;
import com.stacknstock.backend.domain.game.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/runs")
@RequiredArgsConstructor
@Tag(name = "Game Resume", description = "게임 이어하기 API")
public class ResumeController {

    private final ResumeService resumeService;

    @GetMapping("/{runId}/resume")
    @Operation(summary = "게임 이어하기", description = "하루 중간에 종료했던 게임 상태를 조회합니다. 상태 변경은 일어나지 않습니다.")
    public ResponseEntity<DailyStartResponse> resume(
            @Parameter(description = "게임 Run ID", example = "1")
            @PathVariable Long runId
    ) {
        DailyStartResponse response = resumeService.getResumeData(runId);
        return ResponseEntity.ok(response);
    }
}