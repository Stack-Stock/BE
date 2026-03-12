package com.stacknstock.backend.domain.user.controller;

import com.stacknstock.backend.domain.user.dto.UserResponse;
import com.stacknstock.backend.domain.user.entity.User;
import com.stacknstock.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "사용자 정보 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회")
    public ResponseEntity<UserResponse> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        User user = userDetails.getUser();

        UserResponse response = new UserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getNickname()
        );

        return ResponseEntity.ok(response);
    }
}