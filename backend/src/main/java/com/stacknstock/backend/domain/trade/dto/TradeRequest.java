package com.stacknstock.backend.domain.trade.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "주식 거래 요청")
public record TradeRequest(

        @Schema(description = "주문 목록")
        List<TradeOrderDto> orders

) {}