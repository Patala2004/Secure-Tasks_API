package com.indramind.cybersec.secure_tasks_api.service;

import com.indramind.cybersec.secure_tasks_api.dto.ProjectDTO;
import com.indramind.cybersec.secure_tasks_api.dto.UserDTO;
import com.indramind.cybersec.secure_tasks_api.entity.AppUser;
import com.indramind.cybersec.secure_tasks_api.entity.Project;
import com.indramind.cybersec.secure_tasks_api.exceptions.CollaboratorAlreadyExistsException;
import com.indramind.cybersec.secure_tasks_api.exceptions.CollaboratorNotFound;
import com.indramind.cybersec.secure_tasks_api.exceptions.ResourceNotFoundException;
import com.indramind.cybersec.secure_tasks_api.mapper.ProjectMapper;
import com.indramind.cybersec.secure_tasks_api.mapper.UserMapper;
import com.indramind.cybersec.secure_tasks_api.repository.ProjectRepository;
import com.indramind.cybersec.secure_tasks_api.service.impl.ProjectServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository repository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private ProjectServiceImpl projectService;

    @Test
    void getById_shouldReturnProject() {
        Project project = new Project();
        project.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(project));

        Project result = projectService.getById(1L);

        assertEquals(1L, result.getId());
        verify(repository).findById(1L);
    }

    @Test
    void getById_shouldThrowException_whenNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> projectService.getById(1L));
    }

    @Test
    void create_shouldReturnProjectDTO() {
        Long ownerId = 10L;

        AppUser owner = new AppUser();
        owner.setId(ownerId);

        ProjectDTO inputDto = new ProjectDTO();
        inputDto.setName("Test Project");

        Project savedProject = new Project();
        savedProject.setName("Test Project");

        ProjectDTO expectedDto = new ProjectDTO();
        expectedDto.setName("Test Project");

        when(userService.getById(ownerId)).thenReturn(owner);
        when(repository.save(any(Project.class))).thenReturn(savedProject);
        when(projectMapper.toDto(savedProject)).thenReturn(expectedDto);

        ProjectDTO result = projectService.create(inputDto, ownerId);

        assertEquals("Test Project", result.getName());
        verify(repository).save(any(Project.class));
    }

    @Test
    void update_shouldModifyProjectName() {
        Long projectId = 1L;

        Project project = new Project();
        project.setName("Old Name");

        ProjectDTO input = new ProjectDTO();
        input.setName("New Name");

        Project updated = new Project();
        updated.setName("New Name");

        ProjectDTO expected = new ProjectDTO();
        expected.setName("New Name");

        when(repository.findById(projectId)).thenReturn(Optional.of(project));
        when(repository.save(project)).thenReturn(updated);
        when(projectMapper.toDto(updated)).thenReturn(expected);

        ProjectDTO result = projectService.update(input, projectId);

        assertEquals("New Name", result.getName());
        verify(repository).save(project);
    }

    @Test
    void update_shouldIgnoreBlankName() {
        Long projectId = 1L;

        Project project = new Project();
        project.setName("Old Name");

        ProjectDTO input = new ProjectDTO();
        input.setName("   "); // blank

        when(repository.findById(projectId)).thenReturn(Optional.of(project));
        when(repository.save(project)).thenReturn(project);
        when(projectMapper.toDto(project)).thenReturn(new ProjectDTO());

        projectService.update(input, projectId);

        assertEquals("Old Name", project.getName());
        verify(repository).save(project);
    }

    @Test
    void update_shouldIgnoreNullName() {
        Long projectId = 1L;

        Project project = new Project();
        project.setName("Old Name");

        ProjectDTO input = new ProjectDTO();
        input.setName(null); // blank

        when(repository.findById(projectId)).thenReturn(Optional.of(project));
        when(repository.save(project)).thenReturn(project);
        when(projectMapper.toDto(project)).thenReturn(new ProjectDTO());

        projectService.update(input, projectId);

        assertEquals("Old Name", project.getName());
        verify(repository).save(project);
    }


    @Test
    void getAllFromOwner_shouldReturnProjects() {
        Long userId = 1L;

        Project project = new Project();
        project.setId(1L);

        when(repository.findByOwnerId(userId)).thenReturn(List.of(project));
        when(projectMapper.toDto(project)).thenReturn(new ProjectDTO());

        List<ProjectDTO> result = projectService.getAllFromOwner(userId);

        assertEquals(1, result.size());
        verify(repository).findByOwnerId(userId);
    }


    @Test
    void getAllFromUser_shouldReturnProjects() {
        Long userId = 1L;

        Project project = new Project();
        project.setId(1L);

        when(repository.findAllProjectsForUser(userId)).thenReturn(List.of(project));
        when(projectMapper.toDto(project)).thenReturn(new ProjectDTO());

        List<ProjectDTO> result = projectService.getAllFromUser(userId);

        assertEquals(1, result.size());
        verify(repository).findAllProjectsForUser(userId);
    }


    @Test
    void delete_shouldRemoveProject() {
        Project project = new Project();

        when(repository.findById(1L)).thenReturn(Optional.of(project));

        projectService.delete(1L);

        verify(repository).delete(project);
    }


    @Test
    void getCollaborators_shouldReturnUsers() {
        Long projectId = 1L;

        AppUser user = new AppUser();
        user.setId(2L);

        Project project = new Project();
        project.setCollaborators(new HashSet<>(Set.of(user)));

        when(repository.findById(projectId)).thenReturn(Optional.of(project));
        when(userMapper.toDto(user)).thenReturn(new UserDTO());

        List<UserDTO> result = projectService.getCollaborators(projectId);

        assertEquals(1, result.size());
        verify(repository).findById(projectId);
    }

    @Test
    void addCollaborator_shouldAddAndSave() {
        Long projectId = 1L;
        Long userId = 2L;

        AppUser user = new AppUser();
        user.setId(userId);

        Project project = new Project();
        project.setCollaborators(new HashSet<>());

        UserDTO dto = new UserDTO();

        when(repository.findById(projectId)).thenReturn(Optional.of(project));
        when(userService.getById(userId)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(dto);

        UserDTO result = projectService.addCollaborator(projectId, userId);

        assertNotNull(result);
        assertTrue(project.getCollaborators().contains(user));
        verify(repository).save(project);
    }

    @Test
    void removeCollaborator_shouldRemoveAndSave() {
        Long projectId = 1L;
        Long userId = 2L;

        AppUser user = new AppUser();
        user.setId(userId);

        Project project = new Project();
        project.setCollaborators(new HashSet<>(Set.of(user)));

        UserDTO dto = new UserDTO();

        when(repository.findById(projectId)).thenReturn(Optional.of(project));
        when(userService.getById(userId)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(dto);

        UserDTO result = projectService.removeCollaborator(projectId, userId);

        assertNotNull(result);
        assertTrue(!project.getCollaborators().contains(user));
        verify(repository).save(project);
    }

    @Test
    void removeCollaborator_shouldThrow_whenCollaboratorNotFound() {
        Long projectId = 1L;
        Long userId = 2L;

        AppUser user = new AppUser();
        user.setId(userId);

        Project project = new Project();
        project.setCollaborators(new HashSet<>());

        when(repository.findById(projectId)).thenReturn(Optional.of(project));
        when(userService.getById(userId)).thenReturn(user);

        assertThrows(CollaboratorNotFound.class,
                () -> projectService.removeCollaborator(projectId, userId));
    }

    @Test
    void update_shouldThrow_whenProjectNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        ProjectDTO dto = new ProjectDTO();
        dto.setName("New Name");

        assertThrows(ResourceNotFoundException.class,
                () -> projectService.update(dto, 1L));
    }

    @Test
    void delete_shouldThrow_whenProjectNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> projectService.delete(1L));

        verify(repository, never()).delete(any());
    }

    @Test
    void getCollaborators_shouldThrow_whenProjectNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> projectService.getCollaborators(1L));
    }

    @Test
    void addCollaborator_shouldThrow_whenAlreadyExists() {
        Long projectId = 1L;
        Long userId = 2L;

        AppUser user = new AppUser();
        user.setId(userId);

        Project project = new Project();
        project.setCollaborators(new HashSet<>(Set.of(user)));

        when(repository.findById(projectId)).thenReturn(Optional.of(project));
        when(userService.getById(userId)).thenReturn(user);

        assertThrows(CollaboratorAlreadyExistsException.class,
                () -> projectService.addCollaborator(projectId, userId));

        verify(repository, never()).save(any());
    }
}