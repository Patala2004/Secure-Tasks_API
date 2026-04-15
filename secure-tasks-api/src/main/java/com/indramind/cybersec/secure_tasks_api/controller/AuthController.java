package com.indramind.cybersec.secure_tasks_api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.*;

import com.indramind.cybersec.secure_tasks_api.dto.LoginRequest;
import com.indramind.cybersec.secure_tasks_api.dto.RegisterRequest;
import com.indramind.cybersec.secure_tasks_api.dto.UserDTO;
import com.indramind.cybersec.secure_tasks_api.mapper.UserMapper;
import com.indramind.cybersec.secure_tasks_api.service.impl.AuthServiceImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authService;

	private final UserMapper userMapper;

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/register")
    public String register(HttpServletRequest request, @RequestBody @Valid RegisterRequest body) {
        log.info("Register attempt: email={}, ip={}, correlationId={}", body.getEmail(), request.getRemoteAddr(), MDC.get("correlationId"));
        return authService.register(body);
    }

    @PostMapping("/login")
    public String login(HttpServletRequest request, @RequestBody @Valid LoginRequest body) {
        log.info("Login attempt: email={}, ip={}, correlationId={}", body.getEmail(), request.getRemoteAddr(), MDC.get("correlationId"));
        return authService.login(body.getEmail(), body.getPassword());
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, @RequestHeader("Authorization") String token) {
        log.info("Logout attempt: ip={}, correlationId={}", request.getRemoteAddr(), MDC.get("correlationId"));
        throw new UnsupportedOperationException();
    }

    @GetMapping("/me")
    public UserDTO me(HttpServletRequest request, @RequestHeader("Authorization") String token) {
        log.info("Get current user attempt, ip={}, correlationId={}", request.getRemoteAddr(), MDC.get("correlationId"));
        return userMapper.toDto(authService.getCurrentUser(token));
    }
}