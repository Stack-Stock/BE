package com.stacknstock.backend.domain.game.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // 👈 추가

import java.time.LocalDateTime;

// 💡 [핵심] DTO에 정의되지 않은 필드(final_score 등)가 들어와도 에러 내지 말고 무시해라!
@JsonIgnoreProperties(ignoreUnknown = true)
public record ArticleJsonDto(
        Long article_id,
        String title,
        String url,

        @JsonProperty("publisted_at")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime publishedAt
) {}