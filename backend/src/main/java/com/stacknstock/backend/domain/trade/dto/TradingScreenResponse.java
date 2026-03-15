package com.stacknstock.backend.domain.trade.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "주식 거래 화면 데이터")
public record TradingScreenResponse(

        @Schema(description = "현재 보유 현금", example = "1000000")
        BigDecimal cashBalance,

        @Schema(description = "보유 주식 평가금", example = "2500000")
        BigDecimal stockValue,

        @Schema(description = "총 자산", example = "3500000")
        BigDecimal totalAsset,

        @Schema(description = "거래 가능한 종목 리스트")
        List<TradingStockResponse> stocks
) {}