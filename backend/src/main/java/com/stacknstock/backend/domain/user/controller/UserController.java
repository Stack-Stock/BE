package com.stacknstock.backend.domain.user.controller;

import com.stacknstock.backend.domain.user.dto.UserResponse;
import com.stacknstock.backend.domain.user.service.UserService;
import com.stacknstock.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "유저", description = "사용자 정보 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회")
    public ResponseEntity<UserResponse> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        Long userId = userDetails.getUser().getUserId();

        UserResponse response = userService.getMyInfo(userId);

        return ResponseEntity.ok(response);
    }
}