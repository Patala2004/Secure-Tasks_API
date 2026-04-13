package com.indramind.cybersec.secure_tasks_api.repository;

import com.indramind.cybersec.secure_tasks_api.entity.AppUser;
import com.indramind.cybersec.secure_tasks_api.entity.Project;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TestEntityManager entityManager;

    private AppUser createUser(String email) {
        AppUser user = AppUser.builder()
                .username("testuser")
                .email(email)
                .password("password123")
                .build();
        return entityManager.persist(user);
    }

    private Project createProject(AppUser owner, String name) {
        Project project = Project.builder()
                .owner(owner)
                .name(name)
                .collaborators(new HashSet<>())
                .build();
        return entityManager.persist(project);
    }

    @Test
    void findByOwnerId_shouldReturnProjects() {
        AppUser owner = createUser("owner@test.com");
        createProject(owner, "Project A");
        createProject(owner, "Project B");

        List<Project> result = projectRepository.findByOwnerId(owner.getId());

        assertEquals(2, result.size());
    }

    @Test
    void findByOwnerIdAndName_shouldReturnProject() {
        AppUser owner = createUser("owner@test.com");
        createProject(owner, "Project A");

        Optional<Project> result =
                projectRepository.findByOwnerIdAndName(owner.getId(), "Project A");

        assertTrue(result.isPresent());
        assertEquals("Project A", result.get().getName());
    }

    @Test
    void findByCollaboratorsId_shouldReturnProjects() {
        AppUser owner = createUser("owner@test.com");
        AppUser collaborator = createUser("collab@test.com");

        Project project = createProject(owner, "Project A");
        project.addCollaborator(collaborator);

        entityManager.persist(project);

        List<Project> result =
                projectRepository.findByCollaboratorsId(collaborator.getId());

        assertEquals(1, result.size());
    }

    @Test
    void findAllProjectsForUser_shouldReturnOwnedAndCollaborated() {
        AppUser owner = createUser("owner@test.com");
        AppUser collaborator = createUser("collab@test.com");

        createProject(owner, "Owned Project");

        Project shared = createProject(owner, "Shared Project");
        shared.addCollaborator(collaborator);

        entityManager.persist(shared);

        List<Project> result =
                projectRepository.findAllProjectsForUser(collaborator.getId());

        assertEquals(1, result.size());
        assertEquals("Shared Project", result.get(0).getName());
    }
}