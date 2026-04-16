package com.indramind.cybersec.secure_tasks_api.service.impl;

import com.indramind.cybersec.secure_tasks_api.mapper.ProjectMapper;
import com.indramind.cybersec.secure_tasks_api.mapper.UserMapper;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
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

	private static final String NOT_FOUND_MESSAGE = "Project not found";

	@PreAuthorize("@projectSecurity.canAccessProject(#id, authentication.principal.id)")
	public Project getById(Long id){
		return repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MESSAGE));
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
	@PreAuthorize("@projectSecurity.canAccessProject(#id, authentication.principal.id)")
	public ProjectDTO update(@Valid ProjectDTO dto, Long id){
		Project project = repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MESSAGE));
		
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
	@PreAuthorize("@projectSecurity.canAccessProject(#id, authentication.principal.id)")
	public void delete(Long id){
		Project project = repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MESSAGE));

		repository.delete(project);
	}
	
	@PreAuthorize("@projectSecurity.canAccessProject(#id, authentication.principal.id)")
	public List<UserDTO> getCollaborators(Long projectId){
		Project project = repository.findById(projectId)
			.orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MESSAGE));

		return project.getCollaborators()
			.stream().map(user -> userMapper.toDto(user)).toList();
	}

	@Transactional
	@PreAuthorize("@projectSecurity.canAccessProject(#id, authentication.principal.id)")
	public UserDTO addCollaborator(Long projectId, Long userId){
		Project project = repository.findById(projectId)
			.orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MESSAGE));

		AppUser user = userService.getById(userId);
		if(project.getCollaborators().contains(user)){
			throw new CollaboratorAlreadyExistsException(String.format("User with id %d is already a collaborator in project %d", userId, projectId));
		}
		project.addCollaborator(user);
		repository.save(project);
		return userMapper.toDto(user);
	}

	@Transactional
	@PreAuthorize("@projectSecurity.canAccessProject(#id, authentication.principal.id)")
	public UserDTO removeCollaborator(Long projectId, Long userId){
		Project project = repository.findById(projectId)
			.orElseThrow(() -> new ResourceNotFoundException(NOT_FOUND_MESSAGE));

		AppUser user = userService.getById(userId);
		if(!project.getCollaborators().contains(user)){
			throw new CollaboratorNotFound(String.format("User with id %d not found as a collaborator in project %d", userId, projectId));
		}
		project.removeCollaborator(user);
		repository.save(project);
		return userMapper.toDto(user);
	}
}
