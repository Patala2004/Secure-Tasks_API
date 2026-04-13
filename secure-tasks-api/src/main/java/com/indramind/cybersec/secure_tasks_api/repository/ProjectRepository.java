package com.indramind.cybersec.secure_tasks_api.repository;

import com.indramind.cybersec.secure_tasks_api.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
public interface ProjectRepository extends JpaRepository<Project, Long> {
	List<Project> findByOwnerId(Long userId);
	Optional<Project> findByOwnerIdAndName(Long userId, String name);
	List<Project> findByCollaboratorsId(Long userId);
    @Query("""
        SELECT DISTINCT p FROM Project p
        LEFT JOIN p.collaborators c
        WHERE p.owner.id = :userId OR c.id = :userId
    """)
	List<Project> findAllProjectsForUser(@Param("userId") Long userId);
}
