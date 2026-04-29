package com.indramind.cybersec.secure_tasks_api.controller;

import com.indramind.cybersec.secure_tasks_api.dto.ProjectDTO;
import com.indramind.cybersec.secure_tasks_api.dto.UserDTO;
import com.indramind.cybersec.secure_tasks_api.entity.Project;
import com.indramind.cybersec.secure_tasks_api.mapper.ProjectMapper;
import com.indramind.cybersec.secure_tasks_api.service.ProjectService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping(value = "/api/projects", produces = "application/json")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService service;
    private final ProjectMapper mapper;

    @PostMapping(consumes = "application/json")
    public ProjectDTO create(
            @RequestBody @Valid ProjectDTO dto,
            @RequestParam @Min(1) Long ownerId
    ) {
        return service.create(dto, ownerId);
    }

    @GetMapping
    public List<ProjectDTO> getAllFromUser(@RequestParam @Min(1) Long userId) {
        return service.getAllFromUser(userId);
    }

    @GetMapping("/{id}")
    public ProjectDTO getById(@PathVariable @Min(1) Long id) {
        Project project = service.getById(id);
        return mapper.toDto(project);
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    public ProjectDTO update(
            @PathVariable @Min(1) Long id,
            @RequestBody @Valid ProjectDTO dto
    ) {
        return service.update(dto, id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable @Min(1) Long id) {
        service.delete(id);
    }

    @GetMapping("/{id}/collaborators")
    public List<UserDTO> getCollaborators(@PathVariable @Min(1) Long id) {
        return service.getCollaborators(id);
    }

    @PostMapping("/{projectId}/collaborators/{userId}")
    public UserDTO addCollaborator(
            @PathVariable @Min(1) Long projectId,
            @PathVariable @Min(1) Long userId
    ) {
        return service.addCollaborator(projectId, userId);
    }

    @DeleteMapping("/{projectId}/collaborators/{userId}")
    public UserDTO removeCollaborator(
            @PathVariable @Min(1) Long projectId,
            @PathVariable @Min(1) Long userId
    ) {
        return service.removeCollaborator(projectId, userId);
    }
}