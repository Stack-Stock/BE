package com.stacknstock.backend.domain.trade.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "누적 거래 로그 정보")
public record TradeLogResponse(

        @Schema(description = "거래 ID", example = "101")
        Long tradeId,

        @Schema(description = "거래 일차", example = "4")
        Integer dayNo,

        @Schema(description = "종목 ID", example = "10")
        Long stockId,

        @Schema(description = "회사명", example = "삼성전자")
        String company,

        @Schema(description = "거래 타입", example = "BUY")
        String tradeType,

        @Schema(description = "거래 수량", example = "3")
        Long quantity,

        @Schema(description = "체결 가격", example = "72000")
        BigDecimal price,

        @Schema(description = "총 거래 금액", example = "216000")
        BigDecimal totalAmount
) {}
