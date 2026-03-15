package com.stacknstock.backend.domain.game.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "전날 게임 결과 요약")
public record DaySummaryResponse(

        @Schema(description = "전날 일차", example = "3")
        Integer previousDay,

        @Schema(description = "총 자산 변화", example = "150000")
        BigDecimal assetChange,

        @Schema(description = "현금 변화", example = "-50000")
        BigDecimal cashChange,

        @Schema(description = "주식 평가금 변화", example = "200000")
        BigDecimal stockValueChange
) {}