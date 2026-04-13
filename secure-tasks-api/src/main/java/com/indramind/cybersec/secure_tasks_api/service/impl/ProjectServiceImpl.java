package com.indramind.cybersec.secure_tasks_api.service.impl;

import com.indramind.cybersec.secure_tasks_api.mapper.ProjectMapper;
import com.indramind.cybersec.secure_tasks_api.mapper.UserMapper;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.indramind.cybersec.secure_tasks_api.dto.ProjectDTO;
import com.indramind.cybersec.secure_tasks_api.dto.UserDTO;
import com.indramind.cybersec.secure_tasks_api.entity.AppUser;
import com.indramind.cybersec.secure_tasks_api.entity.Project;
import com.indramind.cybersec.secure_tasks_api.exceptions.CollaboratorAlreadyExistsException;
import com.indramind.cybersec.secure_tasks_api.exceptions.CollaboratorNotFound;
import com.indramind.cybersec.secure_tasks_api.exceptions.ResourceNotFoundException;
import com.indramind.cybersec.secure_tasks_api.repository.ProjectRepository;
import com.indramind.cybersec.secure_tasks_api.service.ProjectService;
import com.indramind.cybersec.secure_tasks_api.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectServiceImpl implements ProjectService{

	private final UserMapper userMapper;
	private final ProjectMapper mapper;
	private final ProjectRepository repository;
	private final UserService userService;


	public Project getById(Long id){
		return repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Project not found"));
	}

	@Transactional
	public ProjectDTO create(@Valid ProjectDTO dto, Long ownerId){
		Project project = new Project();
		project.setOwner(
			userService.getById(ownerId)
		);
		project.setName(dto.getName());
		return mapper.toDto(repository.save(project));
	}

	@Transactional
	public ProjectDTO update(@Valid ProjectDTO dto, Long id){
		Project project = repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Project not found"));
		String newName = dto.getName();
		if(newName != null && !newName.isBlank()) project.setName(newName);
		return mapper.toDto(repository.save(project));
	}

	public List<ProjectDTO> getAllFromOwner(Long userId){
		return repository.findByOwnerId(userId).stream().map(mapper::toDto).toList();
	}

	public List<ProjectDTO> getAllFromUser(Long userId){
		return repository.findAllProjectsForUser(userId).stream().map(mapper::toDto).toList();
	}

	@Transactional
	public void delete(Long id){
		Project project = getById(id);
		repository.delete(project);
	}
	
	public List<UserDTO> getCollaborators(Long projectId){
		Project project = getById(projectId);
		return project.getCollaborators()
			.stream().map(user -> userMapper.toDto(user)).toList();
	}

	@Transactional
	public UserDTO addCollaborator(Long projectId, Long userId){
		Project project = getById(projectId);
		AppUser user = userService.getById(userId);
		if(project.getCollaborators().contains(user)){
			throw new CollaboratorAlreadyExistsException(String.format("User with id %d is already a collaborator in project %d", userId, projectId));
		}
		project.addCollaborator(user);
		repository.save(project);
		return userMapper.toDto(user);
	}

	@Transactional
	public UserDTO removeCollaborator(Long projectId, Long userId){
		Project project = getById(projectId);
		AppUser user = userService.getById(userId);
		if(!project.getCollaborators().contains(user)){
			throw new CollaboratorNotFound(String.format("User with id %d not found as a collaborator in project %d", userId, projectId));
		}
		project.removeCollaborator(user);
		repository.save(project);
		return userMapper.toDto(user);
	}
}
