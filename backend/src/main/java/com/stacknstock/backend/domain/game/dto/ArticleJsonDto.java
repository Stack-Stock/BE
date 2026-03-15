package com.stacknstock.backend.domain.game.dto;

import java.time.LocalDateTime;

public record ArticleJsonDto(

        String title,
        String url,
        LocalDateTime publishedAt
) {}
