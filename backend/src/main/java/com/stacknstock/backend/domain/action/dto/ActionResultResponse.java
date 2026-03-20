package com.stacknstock.backend.domain.action.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "행동 실행 결과 응답")
public record ActionResultResponse(

        @Schema(description = "현재 일차", example = "3")
        Integer dayNo,

        @Schema(description = "남은 AP", example = "1")
        Integer apRemaining,

        @Schema(description = "현재 보유 현금", example = "9800000")
        BigDecimal cashBalance,

        @Schema(description = "행동 결과 메시지", example = "휴대폰으로 오늘의 뉴스를 확인했습니다.")
        String message,

        @Schema(description = "엔딩 타입 (1~6, 엔딩이 아닐 경우 null)", example = "1", nullable = true)
        Integer endingType
) {
}