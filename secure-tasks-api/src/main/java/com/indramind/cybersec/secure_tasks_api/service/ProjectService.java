package com.indramind.cybersec.secure_tasks_api.service;

import com.indramind.cybersec.secure_tasks_api.dto.UserDTO;
import com.indramind.cybersec.secure_tasks_api.dto.ProjectDTO;
import com.indramind.cybersec.secure_tasks_api.entity.Project;

import java.util.List;

public interface ProjectService {
	ProjectDTO create(ProjectDTO project, Long creatorId);
	ProjectDTO update(ProjectDTO project, Long id, Long currUserId);
	List<ProjectDTO> getAllFromOwner(Long userId);
	List<ProjectDTO> getAllFromUser(Long userId);
	void delete(Long id, Long currUserId);

	List<UserDTO> getCollaborators(Long projectId, Long currUserId);
	UserDTO addCollaborator(Long projectId, Long userId, Long currUserId);
	UserDTO removeCollaborator(Long projectId, Long userId, Long currUserId);

	Project getById(Long id, Long currUserId);
}