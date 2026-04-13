package com.indramind.cybersec.secure_tasks_api.service;

import com.indramind.cybersec.secure_tasks_api.dto.UserDTO;
import com.indramind.cybersec.secure_tasks_api.dto.ProjectDTO;
import com.indramind.cybersec.secure_tasks_api.entity.Project;

import java.util.List;

public interface ProjectService {
	ProjectDTO create(ProjectDTO project, Long creatorId);
	ProjectDTO update(ProjectDTO project, Long id);
	List<ProjectDTO> getAllFromOwner(Long userId);
	List<ProjectDTO> getAllFromUser(Long userId);
	void delete(Long id);

	List<UserDTO> getCollaborators(Long projectId);
	UserDTO addCollaborator(Long projectId, Long userId);
	UserDTO removeCollaborator(Long projectId, Long userId);

	Project getById(Long id);
}




// public interface UserService {
//     AppUser create(UserPassDTO request);
//     List<AppUser> getAll();
//     AppUser getById(Long id);
//     void delete(Long id);
//     AppUser update(UserDTO user, Long id);
// }