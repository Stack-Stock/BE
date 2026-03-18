package com.stacknstock.backend.global.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "에러 응답 DTO")
public record ErrorResponse(

        @Schema(description = "에러 코드", example = "USER_NOT_FOUND")
        String code,

        @Schema(description = "에러 메시지", example = "사용자를 찾을 수 없습니다.")
        String message
) {}
