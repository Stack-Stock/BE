package com.stacknstock.backend.domain.game.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "기사 정보")
public record ArticleResponse(

        @Schema(description = "기사 케이스 ID", example = "10")
        Long caseId,

        @Schema(description = "기사 제목", example = "현대차 전기차 시장 점유율 확대")
        String title,

        @Schema(description = "기사 발행 날짜", example = "22025-03-24T17:51:09")
        LocalDateTime publishedAt,

        @Schema(description = "원문 기사 링크", example = "https://news.example.com/article")
        String url,

        @Schema(description = "게임 내 스토리")
        String story,

        @Schema(description = "발생 일차", example = "3")
        Integer dayNo,

        @Schema(description = "관련 종목 ID", example = "1")
        Long stockId
) {}