package com.indramind.cybersec.secure_tasks_api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.indramind.cybersec.secure_tasks_api.dto.UserDTO;
import com.indramind.cybersec.secure_tasks_api.entity.AppUser;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void toDto_shouldMapCorrectly() {
        AppUser user = new AppUser();
        user.setId(1L);
        user.setUsername("Test");
		user.setEmail("test@test.com");

        UserDTO dto = mapper.toDto(user);

        assertEquals(1L, dto.getId());
        assertEquals("Test", dto.getUsername());
		assertEquals("test@test.com", dto.getEmail());
    }

    @Test
    void toDto_shouldReturnNull_whenProjectIsNull() {
        assertNull(mapper.toDto(null));
    }

	@Test
    void toEntity_shouldMapCorrectly() {
        UserDTO dto = new UserDTO();
        dto.setId(1L);
        dto.setUsername("Test");
		dto.setEmail("test@test.com");

        AppUser user = mapper.toEntity(dto);

        assertEquals(1L, user.getId());
        assertEquals("Test", user.getUsername());
		assertEquals("test@test.com", user.getEmail());
    }

    @Test
    void toEntity_shouldReturnNull_whenProjectIsNull() {
        assertNull(mapper.toEntity(null));
    }
}