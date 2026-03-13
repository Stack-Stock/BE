package com.stacknstock.backend.domain.game.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이어하기 응답")
public record ContinueRunResponse(

        @Schema(description = "run ID", example = "1")
        Long runId,

        @Schema(description = "현재 일차", example = "5")
        Integer dayNo,

        @Schema(description = "보유 현금", example = "9200000")
        Long cashBalance,

        @Schema(description = "남은 AP", example = "1")
        Integer apRemaining
) {
}