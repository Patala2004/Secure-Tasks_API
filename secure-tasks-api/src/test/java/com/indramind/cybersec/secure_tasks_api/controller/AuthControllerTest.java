package com.indramind.cybersec.secure_tasks_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indramind.cybersec.secure_tasks_api.dto.LoginRequest;
import com.indramind.cybersec.secure_tasks_api.dto.RegisterRequest;
import com.indramind.cybersec.secure_tasks_api.dto.UserDTO;
import com.indramind.cybersec.secure_tasks_api.entity.AppUser;
import com.indramind.cybersec.secure_tasks_api.exceptions.ResourceNotFoundException;
import com.indramind.cybersec.secure_tasks_api.mapper.UserMapper;
import com.indramind.cybersec.secure_tasks_api.service.AuthService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private UserMapper userMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ---------------- REGISTER ----------------

    @Test
    void testRegister_success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
		request.setUsername("newUsername");

        when(authService.register(any())).thenReturn("mock-token");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("mock-token"));
    }

    @Test
    void testRegister_invalidInput() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("invalid-email"); // invalid
        request.setPassword("123"); // too short
		// no password -> required

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ---------------- LOGIN ----------------

    @Test
    void testLogin_success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(authService.login(anyString(), anyString())).thenReturn("jwt-token");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("jwt-token"));
    }

    @Test
    void testLogin_invalidInput() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("invalid-email");
        request.setPassword("");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogout_missingToken() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isBadRequest()); // missing header
    }

    // ---------------- ME ----------------

    @Test
    void testMe_success() throws Exception {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        UserDTO dto = new UserDTO();
        dto.setUsername("testuser");
        dto.setEmail("test@example.com");

        when(authService.getCurrentUser(anyString())).thenReturn(user);
        when(userMapper.toDto(any())).thenReturn(dto);

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testMe_missingToken() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testMe_userNotFound() throws Exception {
        when(authService.getCurrentUser(anyString()))
                .thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNotFound());
    }
}