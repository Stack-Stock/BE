package com.stacknstock.backend.domain.game.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "현재 포트폴리오 조회 응답")
public record PortfolioResponse(

        @Schema(description = "진행 중인 게임 ID", example = "1")
        Long runId,

        @Schema(description = "현재 일차", example = "3")
        Integer currentDayNo,

        @Schema(description = "보유 현금", example = "9800000")
        Long cashBalance,

        @Schema(description = "총 평가 자산", example = "10250000")
        Long totalAssetValue,

        @Schema(description = "보유 종목 리스트")
        List<PortfolioStockResponse> holdings,

        @Schema(description = "누적 거래 로그 목록")
        List<TradeLogResponse> trades

) {
}