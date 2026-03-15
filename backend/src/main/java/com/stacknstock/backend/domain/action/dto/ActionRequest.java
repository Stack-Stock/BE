package com.stacknstock.backend.domain.action.dto;

import com.stacknstock.backend.domain.action.enums.ActionType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "행동 실행 요청")
public record ActionRequest(

        @Schema(description = "행동 타입", example = "INFO_PHONE")
        ActionType actionType,

        @Schema(description = "종목 ID (매수/매도 시 사용)", example = "1", nullable = true)
        Long stockId,

        @Schema(description = "수량 (매수/매도 시 사용)", example = "10", nullable = true)
        Long quantity
) {
}