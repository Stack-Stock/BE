package com.stacknstock.backend.domain.user.controller;

import com.stacknstock.backend.domain.user.dto.LoginRequest;
import com.stacknstock.backend.domain.user.dto.SignUpRequest;
import com.stacknstock.backend.domain.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Tag(name = "Auth", description = "회원가입 및 로그인 API")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;

    /**
     * 회원가입
     */
    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "사용자가 이메일, 닉네임, 비밀번호로 회원가입을 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "이미 존재하는 이메일"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    public ResponseEntity<Void> signUp(
            @RequestBody SignUpRequest request
    ) {

        authService.signUp(request);

        return ResponseEntity.ok().build();
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "로그인 성공 (Session 생성)"),
            @ApiResponse(responseCode = "401", description = "아이디 또는 비밀번호 오류")
    })
    public ResponseEntity<Void> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                );

        Authentication authentication =
                authenticationManager.authenticate(token);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // session 생성
        httpRequest.getSession(true);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "현재 로그인 세션을 종료합니다.")
    @ApiResponse(responseCode = "204", description = "로그아웃 성공")
    public ResponseEntity<Void> logout(HttpServletRequest request) {

        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();

        return ResponseEntity.noContent().build();
    }

}