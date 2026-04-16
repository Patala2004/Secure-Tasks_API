package com.indramind.cybersec.secure_tasks_api.service.impl;

import com.indramind.cybersec.secure_tasks_api.mapper.ProjectMapper;
import com.indramind.cybersec.secure_tasks_api.mapper.UserMapper;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.access.AccessDeniedException;
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
import com.indramind.cybersec.secure_tasks_api.security.CorrelationIdFilter;
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

	private static final Logger log = LoggerFactory.getLogger(ProjectServiceImpl.class);


	public Project getById(Long id, Long currUserId){
		Project proj = repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Project not found"));
		
			AppUser currUser = userService.getById(currUserId);

			if(currUser!=proj.getOwner() && !proj.getCollaborators().contains(currUser)){
				if (log.isWarnEnabled()) log.warn("Unauthorized project getById: project id={}, user id={}, correlationId={}", id, currUserId, MDC.get(CorrelationIdFilter.CORRELATION_KEY));
				throw new AccessDeniedException("Currently logged in user doesn't have access to this project");
			}
			return proj;
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
	public ProjectDTO update(@Valid ProjectDTO dto, Long id, Long currUserId){
		Project project = repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Project not found"));
			
		AppUser currUser = userService.getById(currUserId);

		if(currUser.getId()!=project.getOwner().getId() && !project.getCollaborators().contains(currUser)){
			if (log.isWarnEnabled()) log.warn("Unauthorized project update: project id={}, user id={}, correlationId={}", id, currUserId, MDC.get(CorrelationIdFilter.CORRELATION_KEY));
			throw new AccessDeniedException("Currently logged in user doesn't have access to this project");
		}
		
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
	public void delete(Long id, Long currUserId){
		Project project = repository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Project not found"));

		AppUser currUser = userService.getById(currUserId);

		if(currUser.getId()!=project.getOwner().getId() && !project.getCollaborators().contains(currUser)){
			if (log.isWarnEnabled()) log.warn("Unauthorized project delete: project id={}, user id={}, correlationId={}", id, currUserId, MDC.get(CorrelationIdFilter.CORRELATION_KEY));
			throw new AccessDeniedException("Currently logged in user doesn't have access to this project");
		}

		repository.delete(project);
	}
	
	public List<UserDTO> getCollaborators(Long projectId, Long currUserId){
		Project project = repository.findById(projectId)
			.orElseThrow(() -> new ResourceNotFoundException("Project not found"));

		AppUser currUser = userService.getById(currUserId);

		if(currUser.getId()!=project.getOwner().getId() && !project.getCollaborators().contains(currUser)){
			if (log.isWarnEnabled()) log.warn("Unauthorized project getCollaborators: project id={}, user id={}, correlationId={}", projectId, currUserId, MDC.get(CorrelationIdFilter.CORRELATION_KEY));
			throw new AccessDeniedException("Currently logged in user doesn't have access to this project");
		}

		return project.getCollaborators()
			.stream().map(user -> userMapper.toDto(user)).toList();
	}

	@Transactional
	public UserDTO addCollaborator(Long projectId, Long userId, Long currUserId){
		Project project = repository.findById(projectId)
			.orElseThrow(() -> new ResourceNotFoundException("Project not found"));

		AppUser currUser = userService.getById(currUserId);

		if(currUser.getId()!=project.getOwner().getId() && !project.getCollaborators().contains(currUser)){
			if (log.isWarnEnabled()) log.warn("Unauthorized project addCollaborators: project id={}, user id={}, correlationId={}", projectId, currUserId, MDC.get(CorrelationIdFilter.CORRELATION_KEY));
			throw new AccessDeniedException("Currently logged in user doesn't have access to this project");
		}

		AppUser user = userService.getById(userId);
		if(project.getCollaborators().contains(user)){
			throw new CollaboratorAlreadyExistsException(String.format("User with id %d is already a collaborator in project %d", userId, projectId));
		}
		project.addCollaborator(user);
		repository.save(project);
		return userMapper.toDto(user);
	}

	@Transactional
	public UserDTO removeCollaborator(Long projectId, Long userId, Long currUserId){
		Project project = repository.findById(projectId)
			.orElseThrow(() -> new ResourceNotFoundException("Project not found"));

		AppUser currUser = userService.getById(currUserId);

		if(currUser.getId()!=project.getOwner().getId() && !project.getCollaborators().contains(currUser)){
			if (log.isWarnEnabled()) log.warn("Unauthorized project removeCollaborator: project id={}, user id={}, correlationId={}", projectId, currUserId, MDC.get(CorrelationIdFilter.CORRELATION_KEY));
			throw new AccessDeniedException("Currently logged in user doesn't have access to this project");
		}

		AppUser user = userService.getById(userId);
		if(!project.getCollaborators().contains(user)){
			throw new CollaboratorNotFound(String.format("User with id %d not found as a collaborator in project %d", userId, projectId));
		}
		project.removeCollaborator(user);
		repository.save(project);
		return userMapper.toDto(user);
	}
}
