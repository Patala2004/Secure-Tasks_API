package com.indramind.cybersec.secure_tasks_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indramind.cybersec.secure_tasks_api.dto.ProjectDTO;
import com.indramind.cybersec.secure_tasks_api.dto.UserDTO;
import com.indramind.cybersec.secure_tasks_api.entity.Project;
import com.indramind.cybersec.secure_tasks_api.exceptions.ResourceNotFoundException;
import com.indramind.cybersec.secure_tasks_api.mapper.ProjectMapper;
import com.indramind.cybersec.secure_tasks_api.security.JwtService;
import com.indramind.cybersec.secure_tasks_api.security.UserDetailsServiceImpl;
import com.indramind.cybersec.secure_tasks_api.service.ProjectService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
class ProjectControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService service;

    @MockitoBean
    private ProjectMapper mapper;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String token;

    @BeforeEach
    void setup() {
        token = "Bearer valid-token";

        given(jwtService.isTokenValid(token)).willReturn(true);
        given(jwtService.extractEmail(token)).willReturn("test@test.com");
    }

    // ---------------- CREATE ----------------

    @Test
    void shouldCreateProject() throws Exception {
        ProjectDTO dto = new ProjectDTO(null, "My Project");

        given(service.create(any(), any())).willReturn(dto);

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", token)
                        .param("ownerId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("My Project"));
    }

    @Test
    void shouldFailCreate_invalidInput() throws Exception {
        ProjectDTO dto = new ProjectDTO(null, "abc"); // too short

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", token)
                        .param("ownerId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // ---------------- GET ALL ----------------

    @Test
    void shouldGetProjectsForUser() throws Exception {
        List<ProjectDTO> list = List.of(new ProjectDTO(1L, "Project1"));

        given(service.getAllFromUser(1L)).willReturn(list);

        mockMvc.perform(get("/api/projects")
                        .header("Authorization", token)
                        .param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Project1"));
    }

    // ---------------- GET BY ID ----------------

    @Test
    void shouldGetProjectById() throws Exception {
        Project project = new Project();
        project.setId(1L);
        project.setName("Project1");

        ProjectDTO dto = new ProjectDTO(1L, "Project1");

        given(service.getById(1L)).willReturn(project);
        given(mapper.toDto(project)).willReturn(dto);

        mockMvc.perform(get("/api/projects/1")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Project1"));
    }

    @Test
    void shouldReturn404_whenProjectNotFound() throws Exception {
        given(service.getById(1L))
                .willThrow(new ResourceNotFoundException("Project not found"));

        mockMvc.perform(get("/api/projects/1")
                        .header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    // ---------------- UPDATE ----------------

    @Test
    void shouldUpdateProject() throws Exception {
        ProjectDTO dto = new ProjectDTO(1L, "Updated");

        given(service.update(any(), any())).willReturn(dto);

        mockMvc.perform(put("/api/projects/1")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void shouldFailUpdate_invalidInput() throws Exception {
        ProjectDTO dto = new ProjectDTO(1L, "abc"); // invalid

        mockMvc.perform(put("/api/projects/1")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // ---------------- DELETE ----------------

    @Test
    void shouldDeleteProject() throws Exception {
        mockMvc.perform(delete("/api/projects/1")
                        .header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404_whenDeletingNonExistingProject() throws Exception {
        doThrow(new ResourceNotFoundException("Project not found"))
                .when(service).delete(1L);

        mockMvc.perform(delete("/api/projects/1")
                        .header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    // ---------------- COLLABORATORS ----------------

    @Test
    void shouldGetCollaborators() throws Exception {
        List<UserDTO> users = List.of(new UserDTO());

        given(service.getCollaborators(1L)).willReturn(users);

        mockMvc.perform(get("/api/projects/1/collaborators")
                        .header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAddCollaborator() throws Exception {
        UserDTO user = new UserDTO();

        given(service.addCollaborator(1L, 2L)).willReturn(user);

        mockMvc.perform(post("/api/projects/1/collaborators/2")
                        .header("Authorization", token))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRemoveCollaborator() throws Exception {
        UserDTO user = new UserDTO();

        given(service.removeCollaborator(1L, 2L)).willReturn(user);

        mockMvc.perform(delete("/api/projects/1/collaborators/2")
                        .header("Authorization", token))
                .andExpect(status().isOk());
    }

    // ---------------- SECURITY ----------------

    // @Test
    // void shouldReturnUnauthorized_whenTokenInvalid() throws Exception {
    //     given(jwtService.isTokenValid(token)).willReturn(false);

    //     mockMvc.perform(get("/api/projects")
    //                     .header("Authorization", token)
    //                     .param("userId", "1"))
    //             .andExpect(status().isUnauthorized());
    // }

    // @Test
    // void shouldReturnUnauthorized_whenMissingToken() throws Exception {
    //     mockMvc.perform(get("/api/projects")
    //                     .param("userId", "1"))
    //             .andExpect(status().isUnauthorized());
    // }
}