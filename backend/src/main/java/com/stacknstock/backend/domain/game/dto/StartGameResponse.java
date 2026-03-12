package com.stacknstock.backend.domain.game.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "새 게임 시작 응답")
public record StartGameResponse(

        @Schema(description = "run ID", example = "1")
        Long runId,

        @Schema(description = "현재 일차", example = "1")
        Integer dayNo,

        @Schema(description = "현재 보유 현금", example = "3000000")
        Long cashBalance,

        @Schema(description = "남은 AP", example = "2")
        Integer apRemaining
) {
}