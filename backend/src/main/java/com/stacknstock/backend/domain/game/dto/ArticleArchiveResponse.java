package com.stacknstock.backend.domain.game.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "기사 아카이브 응답")
public record ArticleArchiveResponse(

        @Schema(description = "누적 기사 목록")
        List<ArticleResponse> articles
) {}