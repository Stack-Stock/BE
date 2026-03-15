package com.stacknstock.backend.domain.trade.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "종목 그래프 한 포인트")
public record PricePointResponse(

        @Schema(description = "일차", example = "3")
        Integer dayNo,

        @Schema(description = "해당 일자의 종가", example = "71500")
        BigDecimal closePrice
) {}