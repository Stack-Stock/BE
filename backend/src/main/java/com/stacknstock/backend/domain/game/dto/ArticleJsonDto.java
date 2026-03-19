package com.stacknstock.backend.domain.game.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record ArticleJsonDto(

        String title,
        String url,

        @JsonProperty("publisted_at")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime publishedAt
) {}
