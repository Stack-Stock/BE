package com.stacknstock.backend.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record SignUpRequest(

        @Schema(description = "사용자 이메일", example = "test@test.com")
        String email,

        @Schema(description = "닉네임", example = "stacker")
        String nickname,

        @Schema(description = "비밀번호", example = "1234")
        String password
) {}
