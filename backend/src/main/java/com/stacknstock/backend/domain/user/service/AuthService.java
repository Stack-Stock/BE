package com.stacknstock.backend.domain.user.service;

import com.stacknstock.backend.domain.user.dto.SignUpRequest;
import com.stacknstock.backend.domain.user.entity.User;
import com.stacknstock.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void signUp(SignUpRequest request) {

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        User user = new User(
                request.email(),
                request.nickname(),
                passwordEncoder.encode(request.password())
        );

        userRepository.save(user);
    }
}