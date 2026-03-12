package com.stacknstock.backend.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserResponse(

        @Schema(description = "유저 ID")
        Long userId,

        @Schema(description = "이메일")
        String email,

        @Schema(description = "닉네임")
        String nickname
) {}