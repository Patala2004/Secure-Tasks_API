package com.indramind.cybersec.secure_tasks_api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

import com.indramind.cybersec.secure_tasks_api.entity.AppUser;
import com.indramind.cybersec.secure_tasks_api.security.JwtService;
import com.indramind.cybersec.secure_tasks_api.security.UserDetailsServiceImpl;
import com.indramind.cybersec.secure_tasks_api.service.UserService;

import java.util.List;

@WebMvcTest(UserController.class)
class UserControllerMvcTest {

    @Autowired 
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private List<AppUser> users;
    private String rawToken;
    private AppUser testUserEntity;

    @BeforeEach
    void setup() {
        rawToken = "Bearer faketoken";

        testUserEntity = new AppUser(1L, "Alice", "alice@test.com", "hashedpassword");

        given(jwtService.isTokenValid(rawToken)).willReturn(true);
        given(jwtService.extractEmail(rawToken)).willReturn(testUserEntity.getEmail());
    }

    @Test
    void shouldReturnAllUsers() throws Exception{
        users = List.of(
            new AppUser(1L, "Alicia", "alicia@minsait.com", "passwd"), 
            new AppUser(1L, "Salomon", "salomon@minsait.com", "passwd")
        );
        
        given(userService.getAll()).willReturn(users);

        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + rawToken)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(users)));
    }
}
