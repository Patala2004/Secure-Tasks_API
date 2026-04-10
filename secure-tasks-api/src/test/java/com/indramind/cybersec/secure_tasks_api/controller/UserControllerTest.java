package com.indramind.cybersec.secure_tasks_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indramind.cybersec.secure_tasks_api.dto.UserPassDTO;
import com.indramind.cybersec.secure_tasks_api.entity.AppUser;
import com.indramind.cybersec.secure_tasks_api.repository.UserRepository;
import com.indramind.cybersec.secure_tasks_api.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@AutoConfigureMockMvc
@SpringBootTest()
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

	@MockitoBean
	private JwtService jwtService;

    private final ObjectMapper objectMapper = new ObjectMapper();
	
    @BeforeEach
    void setUp() {
        userRepository.deleteAll(); // Clean DB before each test

		// Mock JwtService to always validate any token
		when(jwtService.isTokenValid(anyString(), any())).thenReturn(true);

		// Mock JwtService to always extract a fixed username
		when(jwtService.extractEmail(anyString())).thenReturn("testuser@example.com");
    }

    @Test
    void testCreateUser() throws Exception {
        UserPassDTO request = new UserPassDTO();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setEmail("testuser@example.com");

		when(jwtService.generateAccessToken(any())).thenReturn("MockJWTTocken");

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
				.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("MockJWTTocken"));

        // Verify saved in DB
        AppUser savedUser = userRepository.findByUsername("testuser").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("testuser@example.com");
    }

    @Test
    void testGetAllUsers() throws Exception {
        AppUser user = new AppUser();
        user.setUsername("user1");
        user.setPassword("password123");
        user.setEmail("testuser@example.com");
        userRepository.save(user);

        mockMvc.perform(get("/api/users").header("Authorization", "Bearer dummy-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].username").value("user1"));
    }

    @Test
    void testGetUserById() throws Exception {
        AppUser user = new AppUser();
        user.setUsername("user2");
        user.setPassword("password123");
        user.setEmail("testuser@example.com");
        AppUser savedUser = userRepository.save(user);

        mockMvc.perform(get("/api/users/{id}", savedUser.getId()).header("Authorization", "Bearer dummy-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user2"));
    }

    @Test
    void testDeleteUser() throws Exception {
        AppUser user = new AppUser();
        user.setUsername("user3");
        user.setPassword("password123");
        user.setEmail("testuser@example.com");
        AppUser savedUser = userRepository.save(user);

		

        mockMvc.perform(delete("/api/users/{id}", savedUser.getId()).header("Authorization", "Bearer dummy-token"))
                .andExpect(status().isOk());
				

        assertThat(userRepository.findById(savedUser.getId())).isEmpty();
    }
}