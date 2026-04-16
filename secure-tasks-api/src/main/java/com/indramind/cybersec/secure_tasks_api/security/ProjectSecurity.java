package com.indramind.cybersec.secure_tasks_api.security;

import org.springframework.stereotype.Component;

import com.indramind.cybersec.secure_tasks_api.entity.AppUser;
import com.indramind.cybersec.secure_tasks_api.entity.Project;
import com.indramind.cybersec.secure_tasks_api.repository.ProjectRepository;
import com.indramind.cybersec.secure_tasks_api.service.UserService;

import lombok.RequiredArgsConstructor;

@Component("projectSecurity")
@RequiredArgsConstructor
public class ProjectSecurity {

    private final ProjectRepository repository;
    private final UserService userService;

    public boolean canAccessProject(Long projectId, Long userId) {
        Project project = repository.findById(projectId)
                .orElse(null);

        if (project == null) return false;

        AppUser user = userService.getById(userId);

        return project.getOwner().equals(user)
                || project.getCollaborators().contains(user);
    }
}