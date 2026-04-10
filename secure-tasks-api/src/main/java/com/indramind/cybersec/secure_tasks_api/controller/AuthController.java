package com.indramind.cybersec.secure_tasks_api.controller;

import org.springframework.web.bind.annotation.*;

import com.indramind.cybersec.secure_tasks_api.dto.LoginRequest;
import com.indramind.cybersec.secure_tasks_api.dto.RegisterRequest;
import com.indramind.cybersec.secure_tasks_api.dto.UserDTO;
import com.indramind.cybersec.secure_tasks_api.mapper.UserMapper;
import com.indramind.cybersec.secure_tasks_api.service.impl.AuthServiceImpl;

import jakarta.validation.Valid;
import lombok.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authService;

	private final UserMapper userMapper;

    @PostMapping("/register")
    public String register(@RequestBody @Valid RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public String login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request.getEmail(), request.getPassword());
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String token) {
        throw new UnsupportedOperationException();
    }

    @GetMapping("/me")
    public UserDTO me(@RequestHeader("Authorization") String token) {
        return userMapper.toDto(authService.getCurrentUser(token));
    }
}