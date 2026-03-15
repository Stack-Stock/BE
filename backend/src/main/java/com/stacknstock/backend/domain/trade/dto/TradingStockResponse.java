package com.stacknstock.backend.domain.trade.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "거래 화면에 표시되는 종목 정보")
public record TradingStockResponse(

        @Schema(description = "종목 ID", example = "1")
        Long stockId,

        @Schema(description = "회사 이름", example = "삼성전자")
        String company,

        @Schema(description = "내 보유 수량", example = "10")
        Long myQuantity,

        @Schema(description = "현재 가격", example = "72000")
        BigDecimal currentPrice,

        @Schema(description = "전일 대비 등락률", example = "0.0125")
        BigDecimal returnPct,

        @Schema(description = "전일 대비 가격 변동", example = "850")
        BigDecimal priceChange,

        @Schema(description = "게임 시작 이후 누적 가격 그래프 데이터")
        List<PricePointResponse> priceHistory
) {}