package com.indramind.cybersec.secure_tasks_api.controller;

import com.indramind.cybersec.secure_tasks_api.entity.AppUser;
import com.indramind.cybersec.secure_tasks_api.repository.UserRepository;
import com.indramind.cybersec.secure_tasks_api.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private JwtService jwtService;

    private AppUser testUser;
    private String token;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        // Create user that will be "authenticated"
        testUser = new AppUser();
        testUser.setUsername("testuser");
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("password");
        testUser = userRepository.save(testUser);

        token = "Bearer valid-token";

        // Simulate valid token
        when(jwtService.isTokenValid(anyString(), any())).thenReturn(true);
        when(jwtService.extractEmail(anyString())).thenReturn(testUser.getEmail());
    }

    // ---------------- UNAUTHORIZED ----------------

    @Test
    void shouldReturn401_whenNoToken() throws Exception {
        mockMvc.perform(get("/api/projects")
                        .param("userId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401_whenInvalidToken() throws Exception {
        when(jwtService.isTokenValid(anyString(), any())).thenReturn(false);

        mockMvc.perform(get("/api/projects")
                        .header("Authorization", "Bearer invalid-token")
                        .param("userId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401_whenMalformedHeader() throws Exception {
        mockMvc.perform(get("/api/projects")
                        .header("Authorization", "invalid-format")
                        .param("userId", "1"))
                .andExpect(status().isUnauthorized());
    }

    // ---------------- AUTHENTICATED ----------------

    @Test
    void shouldAllowAccess_whenTokenValid() throws Exception {
        mockMvc.perform(get("/api/projects")
                        .header("Authorization", token)
                        .param("userId", "1"))
                .andExpect(status().isOk());
    }

    // ---------------- EDGE CASES ----------------

    @Test
    void shouldReturn401_whenUserDoesNotExist() throws Exception {
        userRepository.deleteAll(); // simulate deleted user

        mockMvc.perform(get("/api/projects")
                        .header("Authorization", token)
                        .param("userId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401_whenTokenHasUnknownEmail() throws Exception {
        when(jwtService.extractEmail(anyString()))
                .thenReturn("unknown@example.com");

        mockMvc.perform(get("/api/projects")
                        .header("Authorization", token)
                        .param("userId", "1"))
                .andExpect(status().isUnauthorized());
    }
}