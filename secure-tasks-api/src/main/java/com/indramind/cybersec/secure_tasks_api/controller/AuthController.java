package com.indramind.cybersec.secure_tasks_api.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.indramind.cybersec.secure_tasks_api.dto.LoginRequest;
import com.indramind.cybersec.secure_tasks_api.dto.RegisterRequest;
import com.indramind.cybersec.secure_tasks_api.dto.UserDTO;
import com.indramind.cybersec.secure_tasks_api.logging.CustomLogger;
import com.indramind.cybersec.secure_tasks_api.logging.impl.CustomLoggerFactory;
import com.indramind.cybersec.secure_tasks_api.mapper.UserMapper;
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

    private static final CustomLogger log = CustomLoggerFactory.getLogger(AuthController.class);

    @PostMapping(value = "/register", consumes = "application/json")
    public String register(HttpServletRequest request, @RequestBody @Valid RegisterRequest body) {
        log.info("Register attempt: ip={}", request.getRemoteAddr());
        String response = authService.register(body);
        log.info("Register attempt finished. New user: {}", authService.getCurrentUser(response).getId());
        return response;
    }

    @PostMapping(value = "/login", consumes = "application/json")
    public String login(HttpServletRequest request, @RequestBody @Valid LoginRequest body) {
        log.info("Login attempt: ip={}", request.getRemoteAddr());
        String response = authService.login(body.getEmail(), body.getPassword());
        log.info("Login attempt finished. Logged user: {}", authService.getCurrentUser(response).getId());
        return response;
    }

    @PostMapping(value = "/logout")
    public void logout(HttpServletRequest request, @RequestHeader("Authorization") String token) {
        log.info("Logout attempt: ip={}", request.getRemoteAddr());
        throw new UnsupportedOperationException();
    }

    @GetMapping("/me")
    public UserDTO me(HttpServletRequest request, @RequestHeader("Authorization") String token) {
        log.info("Get current user attempt, ip={}", request.getRemoteAddr());
        UserDTO response = userMapper.toDto(authService.getCurrentUser(token));
        log.info("Get current user attempt finished. Current user: {}", response.getId());
        return response;
    }
}