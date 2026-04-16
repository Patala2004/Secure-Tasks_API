package com.indramind.cybersec.secure_tasks_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indramind.cybersec.secure_tasks_api.dto.UserDTO;
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

    @Test
    void testUpdateUser() throws Exception {
        // 1. Create initial user
        AppUser user = new AppUser();
        user.setUsername("oldUsername");
        user.setPassword("password123");
        user.setEmail("testuser@example.com");
        AppUser savedUser = userRepository.save(user);

        Long savedUserId = savedUser.getId();

        // 2. Prepare updated data (UserDTO)
        UserDTO updateDto = new UserDTO();
        updateDto.setUsername("newUsername");
        updateDto.setEmail("testuser2@example.com");

        String requestJson = objectMapper.writeValueAsString(updateDto);

        // 3. Perform PUT request
        mockMvc.perform(put("/api/users/{id}", savedUserId)
                        .header("Authorization", "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newUsername"))
                .andExpect(jsonPath("$.email").value("testuser2@example.com"));

        // 4. Verify DB update
        AppUser updatedUser = userRepository.findById(savedUserId).orElse(null);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getUsername()).isEqualTo("newUsername");
        assertThat(updatedUser.getEmail()).isEqualTo("testuser2@example.com");
    }

    @Test
    void testCreateUserWithExistingEmail() throws Exception {
        UserPassDTO request = new UserPassDTO();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setEmail("testuser@example.com");

        UserPassDTO request2 = new UserPassDTO();
        request2.setUsername("testuser2");
        request2.setPassword("password123");
        request2.setEmail("testuser@example.com");

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


        // Request creation of user with same email

        requestJson = objectMapper.writeValueAsString(request2);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
				.andDo(print())
                .andExpect(status().isConflict());

        // Verify NOT saved in DB
        savedUser = userRepository.findByUsername("testuser2").orElse(null);
        assertThat(savedUser).isNull();
    }

    @Test
    void testUpdateUserWithExistingEmail() throws Exception {
        // 1. Create initial users
        AppUser user = new AppUser();
        user.setUsername("oldUsername");
        user.setPassword("password123");
        user.setEmail("testuser@example.com");
        AppUser savedUser = userRepository.save(user);

        AppUser user2 = new AppUser();
        user2.setUsername("oldUsername2");
        user2.setPassword("password123");
        user2.setEmail("oldmail2@example.com");
        userRepository.save(user2);

        // 2. Prepare updated data (UserDTO)
        UserDTO updateDto = new UserDTO();
        updateDto.setUsername("newUsername2");
        updateDto.setEmail("oldmail2@example.com");

        String requestJson = objectMapper.writeValueAsString(updateDto);

        // 3. Perform PUT request
        mockMvc.perform(put("/api/users/{id}", savedUser.getId())
                        .header("Authorization", "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andDo(print())
                .andExpect(status().isConflict());

        // 4. Verify lack of DB update
        AppUser updatedUser = userRepository.findById(savedUser.getId()).orElse(null);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getUsername()).isEqualTo("oldUsername");
        assertThat(updatedUser.getEmail()).isEqualTo("testuser@example.com");
    }

    @Test
    void testUpdateUser_invalidInput() throws Exception {
        // 1. Save testuser so authentication doesn't fail users
        AppUser user = new AppUser();
        user.setUsername("oldUsername");
        user.setPassword("password123");
        user.setEmail("testuser@example.com");
        AppUser savedUser = userRepository.save(user);

        UserDTO dto = new UserDTO(); // missing required fields
        dto.setUsername("a"); // to short of an username

        mockMvc.perform(put("/api/users/{id}", savedUser.getId())
                        .header("Authorization", "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetUserById_notFound() throws Exception {
        AppUser user = new AppUser();
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setPassword("password");
        userRepository.save(user);
        mockMvc.perform(get("/api/users/{id}", 99999L)
                        .header("Authorization", "Bearer dummy-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteUser_notFound() throws Exception {
        AppUser user = new AppUser();
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setPassword("password");
        userRepository.save(user);
        mockMvc.perform(delete("/api/users/{id}", 99999L)
                        .header("Authorization", "Bearer dummy-token"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateUser_invalidEmailFormat() throws Exception {
        AppUser user = new AppUser();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("testuser@example.com");
        AppUser savedUser = userRepository.save(user);

        UserDTO dto = new UserDTO();
        dto.setUsername("newUser");
        dto.setEmail("invalid-email");

        mockMvc.perform(put("/api/users/{id}", savedUser.getId())
                        .header("Authorization", "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateUser_missingEmail() throws Exception { // should not update email
        AppUser user = new AppUser();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("testuser@example.com");
        AppUser savedUser = userRepository.save(user);

        UserDTO dto = new UserDTO();
        dto.setUsername("newUser");
        dto.setEmail(null);

        mockMvc.perform(put("/api/users/{id}", savedUser.getId())
                        .header("Authorization", "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newUser"))
                .andExpect(jsonPath("$.email").value("testuser@example.com"));

        // 4. Verify DB update
        AppUser updatedUser = userRepository.findById(savedUser.getId()).orElse(null);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getUsername()).isEqualTo("newUser");
        assertThat(updatedUser.getEmail()).isEqualTo("testuser@example.com"); // not null
        
    }

    @Test
    void testUpdateUser_missingUsername() throws Exception { // should not update email
        AppUser user = new AppUser();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("testuser@example.com");
        AppUser savedUser = userRepository.save(user);

        UserDTO dto = new UserDTO();
        dto.setUsername(null);
        dto.setEmail("testuser2@example.com");

        mockMvc.perform(put("/api/users/{id}", savedUser.getId())
                        .header("Authorization", "Bearer dummy-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("testuser2@example.com"));

        // 4. Verify DB update
        AppUser updatedUser = userRepository.findById(savedUser.getId()).orElse(null);

        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getUsername()).isEqualTo("testuser"); // not null
        assertThat(updatedUser.getEmail()).isEqualTo("testuser2@example.com"); 
    }

    @Test
    void testGetAllUsers_invalidToken() throws Exception {
        AppUser user = new AppUser();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("testuser@example.com");
        userRepository.save(user);
        when(jwtService.isTokenValid(anyString(), any())).thenReturn(false);

        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdate_invalidToken() throws Exception {
        AppUser user = new AppUser();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("testuser@example.com");
        userRepository.save(user);
        when(jwtService.isTokenValid(anyString(), any())).thenReturn(false);

        mockMvc.perform(put("/api/update/{id}", 1L)
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetUser_invalidToken() throws Exception {
        AppUser user = new AppUser();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("testuser@example.com");
        userRepository.save(user);
        when(jwtService.isTokenValid(anyString(), any())).thenReturn(false);

        mockMvc.perform(get("/api/users/{id}", 1L)
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }
}