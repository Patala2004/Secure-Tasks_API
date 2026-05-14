package com.indramind.cybersec.secure_tasks_api.controller;

import com.indramind.cybersec.secure_tasks_api.dto.ProjectDTO;
import com.indramind.cybersec.secure_tasks_api.dto.UserDTO;
import com.indramind.cybersec.secure_tasks_api.logging.CustomLogger;
import com.indramind.cybersec.secure_tasks_api.logging.impl.CustomLoggerFactory;
import com.indramind.cybersec.secure_tasks_api.mapper.ProjectMapper;
import com.indramind.cybersec.secure_tasks_api.security.utils.SecurityUtils;
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

    private static final CustomLogger log = CustomLoggerFactory.getLogger(ProjectController.class);

    @PostMapping(consumes = "application/json")
    public ProjectDTO create(
            @RequestBody @Valid ProjectDTO dto,
            @RequestParam @Min(1) Long ownerId
    ) {
        log.info("Create Project attempt started. User: {}", SecurityUtils.getLoggedUserId());
        ProjectDTO response = service.create(dto, ownerId);
        log.info("Create Project attempt finished. Project created: {}", response.getId());
        return response;
    }

    @GetMapping
    public List<ProjectDTO> getAllFromUser(@RequestParam @Min(1) Long userId) {
        log.info("Get all projects from user attempt started. User: {}", SecurityUtils.getLoggedUserId());
        List<ProjectDTO> response = service.getAllFromUser(userId);
        log.info("Get all projects from user attempt finished.");
        return response;
    }

    @GetMapping("/{id}")
    public ProjectDTO getById(@PathVariable @Min(1) Long id) {
        log.info("Get project by id attempt started. User: {}", SecurityUtils.getLoggedUserId());
        ProjectDTO response = mapper.toDto(service.getById(id));
        log.info("Get project by id attempt finished.");
        return response;
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    public ProjectDTO update(
            @PathVariable @Min(1) Long id,
            @RequestBody @Valid ProjectDTO dto
    ) {
        log.info("update project attempt started. User: {}", SecurityUtils.getLoggedUserId());
        ProjectDTO response = service.update(dto, id);
        log.info("update project attempt finished.");
        return response;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable @Min(1) Long id) {
        log.info("delete project attempt started. User: {}", SecurityUtils.getLoggedUserId());
        service.delete(id);
        log.info("delete project attempt finished.");
    }

    @GetMapping("/{id}/collaborators")
    public List<UserDTO> getCollaborators(@PathVariable @Min(1) Long id) {
        log.info("get project collaborators attempt started. User: {}", SecurityUtils.getLoggedUserId());
        List<UserDTO> response = service.getCollaborators(id);
        log.info("get project collabortaros attempt finished.");
        return response;
    }

    @PostMapping("/{projectId}/collaborators/{userId}")
    public UserDTO addCollaborator(
            @PathVariable @Min(1) Long projectId,
            @PathVariable @Min(1) Long userId
    ) {
        log.info("add project collaborator attempt started. User: {}", SecurityUtils.getLoggedUserId());
        UserDTO response = service.addCollaborator(projectId, userId);
        log.info("add project collaborator attempt finished.");
        return response;
    }

    @DeleteMapping("/{projectId}/collaborators/{userId}")
    public UserDTO removeCollaborator(
            @PathVariable @Min(1) Long projectId,
            @PathVariable @Min(1) Long userId
    ) {
        log.info("remove project collaborator attempt started. User: {}", SecurityUtils.getLoggedUserId());
        UserDTO response =  service.removeCollaborator(projectId, userId);
        log.info("remove project collaborator attempt finished.");
        return response;
    }
}