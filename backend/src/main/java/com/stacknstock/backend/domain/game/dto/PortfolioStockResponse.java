package com.stacknstock.backend.domain.game.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "포트폴리오 내 개별 보유 종목 정보")
public record PortfolioStockResponse(

        @Schema(description = "종목 ID", example = "1")
        Long stockId,

        @Schema(description = "종목 티커", example = "MKAI")
        String ticker,

        @Schema(description = "회사명", example = "MK AI")
        String companyName,

        @Schema(description = "보유 수량", example = "10")
        Long quantity,

        @Schema(description = "평균 단가", example = "10000")
        Long avgCost,

        @Schema(description = "현재가", example = "12000")
        Long currentPrice,

        @Schema(description = "평가 금액", example = "120000")
        Long evaluationAmount,

        @Schema(description = "평가 손익", example = "20000")
        Long profitLoss

) {
}