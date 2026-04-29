package com.indramind.cybersec.secure_tasks_api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.indramind.cybersec.secure_tasks_api.dto.LoginRequest;
import com.indramind.cybersec.secure_tasks_api.dto.RegisterRequest;
import com.indramind.cybersec.secure_tasks_api.dto.UserDTO;
import com.indramind.cybersec.secure_tasks_api.mapper.UserMapper;
import com.indramind.cybersec.secure_tasks_api.security.CorrelationIdFilter;
import com.indramind.cybersec.secure_tasks_api.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.*;

@RestController
@Validated
@RequestMapping(value = "/auth", produces = "application/json")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

	private final UserMapper userMapper;

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @PostMapping(value = "/register", consumes = "application/json")
    public String register(HttpServletRequest request, @RequestBody @Valid RegisterRequest body) {
        if (log.isInfoEnabled()) log.info("Register attempt: ip={}, correlationId={}", request.getRemoteAddr(), MDC.get(CorrelationIdFilter.CORRELATION_KEY));
        return authService.register(body);
    }

    @PostMapping(value = "/login", consumes = "application/json")
    public String login(HttpServletRequest request, @RequestBody @Valid LoginRequest body) {
        if (log.isInfoEnabled()) log.info("Login attempt: ip={}, correlationId={}", request.getRemoteAddr(), MDC.get(CorrelationIdFilter.CORRELATION_KEY));
        return authService.login(body.getEmail(), body.getPassword());
    }

    @PostMapping(value = "/logout")
    public void logout(HttpServletRequest request, @RequestHeader("Authorization") String token) {
        if (log.isInfoEnabled()) log.info("Logout attempt: ip={}, correlationId={}", request.getRemoteAddr(), MDC.get(CorrelationIdFilter.CORRELATION_KEY));
        throw new UnsupportedOperationException();
    }

    @GetMapping("/me")
    public UserDTO me(HttpServletRequest request, @RequestHeader("Authorization") String token) {
        if (log.isInfoEnabled()) log.info("Get current user attempt, ip={}, correlationId={}", request.getRemoteAddr(), MDC.get(CorrelationIdFilter.CORRELATION_KEY));
        return userMapper.toDto(authService.getCurrentUser(token));
    }
}