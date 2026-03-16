package com.stacknstock.backend.domain.trade.dto;

import com.stacknstock.backend.domain.trade.enums.TradeSide;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "개별 주문 정보")
public record TradeOrderDto(

        @Schema(description = "종목 ID", example = "3")
        Long stockId,

        @Schema(description = "주문 타입 (BUY / SELL)", example = "BUY")
        TradeSide side,

        @Schema(description = "주문 수량", example = "10")
        Long quantity
) {}