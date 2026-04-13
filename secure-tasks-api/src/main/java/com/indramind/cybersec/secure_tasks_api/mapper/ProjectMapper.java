package com.indramind.cybersec.secure_tasks_api.mapper;

import java.util.HashSet;

import org.springframework.stereotype.Component;

import com.indramind.cybersec.secure_tasks_api.dto.ProjectDTO;
import com.indramind.cybersec.secure_tasks_api.entity.AppUser;
import com.indramind.cybersec.secure_tasks_api.entity.Project;

@Component
public class ProjectMapper {
	public ProjectDTO toDto(Project project){
		if(project == null) return null;

		ProjectDTO dto = new ProjectDTO();
		dto.setId(project.getId());
		dto.setName(project.getName());
		return dto;
	}

	public Project toEntity(ProjectDTO dto, AppUser owner){
		if(dto == null) return null;
		if(owner == null) throw new IllegalArgumentException();
		Project project = new Project();
		project.setId(dto.getId());
		project.setName(dto.getName());
		project.setOwner(owner);
		project.setCollaborators(new HashSet<AppUser>());
		return project;
	}
}