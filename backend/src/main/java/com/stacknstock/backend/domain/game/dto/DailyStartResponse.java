package com.stacknstock.backend.domain.game.dto;

import com.stacknstock.backend.domain.trade.dto.TradingScreenResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "하루 시작 시 프론트에 전달되는 전체 데이터")
public record DailyStartResponse(

        @Schema(description = "전날 결산 요약 정보")
        DaySummaryResponse daySummary,

        @Schema(description = "현재 포트폴리오 정보")
        PortfolioResponse portfolio,

        @Schema(description = "지금까지 발생한 기사 목록")
        ArticleArchiveResponse articleArchive,

        @Schema(description = "주식 거래 화면 정보")
        TradingScreenResponse tradingScreen,

        @Schema(description = "오늘 랜덤 이벤트 아이디", example = "1")
        Long randomEventId,

        @Schema(description = "오늘 현금화되는 T+3 정산 금액", example = "120000")
        BigDecimal settlementAmount,

        @Schema(description = "오늘 번뜩임 지급 여부", example = "true")
        Boolean hasInspiration
) {}