package com.indramind.cybersec.secure_tasks_api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.indramind.cybersec.secure_tasks_api.dto.ProjectDTO;
import com.indramind.cybersec.secure_tasks_api.entity.AppUser;
import com.indramind.cybersec.secure_tasks_api.entity.Project;

class ProjectMapperTest {

    private final ProjectMapper mapper = new ProjectMapper();

    @Test
    void toDto_shouldMapCorrectly() {
        Project project = new Project();
        project.setId(1L);
        project.setName("Test");

        ProjectDTO dto = mapper.toDto(project);

        assertEquals(1L, dto.getId());
        assertEquals("Test", dto.getName());
    }

    @Test
    void toDto_shouldReturnNull_whenProjectIsNull() {
        assertNull(mapper.toDto(null));
    }

	@Test
    void toEntity_shouldMapCorrectly() {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(1L);
        dto.setName("Test");
		
		AppUser user = new AppUser();

        Project project = mapper.toEntity(dto, user);

        assertEquals(1L, project.getId());
        assertEquals("Test", project.getName());
		assertEquals(user, project.getOwner());
    }

    @Test
    void toEntity_shouldReturnNull_whenProjectIsNull() {
        assertNull(mapper.toEntity(null, new AppUser()));
    }

	@Test
    void toEntity_shouldThrow_whenOwnerIsNullAndProjectIsnt() {
		ProjectDTO dto = new ProjectDTO();
        dto.setId(1L);
        dto.setName("Test");
        assertThrows(IllegalArgumentException.class, 
			() -> mapper.toEntity(dto, null));
    }
}