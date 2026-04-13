package com.indramind.cybersec.secure_tasks_api.service;

import com.indramind.cybersec.secure_tasks_api.dto.UserDTO;
import com.indramind.cybersec.secure_tasks_api.dto.UserPassDTO;
import com.indramind.cybersec.secure_tasks_api.entity.AppUser;
import com.indramind.cybersec.secure_tasks_api.exceptions.EmailInUseException;
import com.indramind.cybersec.secure_tasks_api.exceptions.ResourceNotFoundException;
import com.indramind.cybersec.secure_tasks_api.repository.UserRepository;
import com.indramind.cybersec.secure_tasks_api.service.impl.UserServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void create_shouldSaveUserWithEncodedPassword() {
        UserPassDTO request = new UserPassDTO();
        request.setUsername("testuser");
        request.setEmail("test@email.com");
        request.setPassword("plainPassword");

        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");

        AppUser savedUser = AppUser.builder()
                .username("testuser")
                .email("test@email.com")
                .password("encodedPassword")
                .build();

        when(repository.save(any(AppUser.class))).thenReturn(savedUser);

        AppUser result = userService.create(request);

        assertEquals("testuser", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());

        verify(passwordEncoder).encode("plainPassword");
        verify(repository).save(any(AppUser.class));
    }

    @Test
    void getAll_shouldReturnUsers() {
        AppUser user = new AppUser();
        user.setId(1L);

        when(repository.findAll()).thenReturn(List.of(user));

        List<AppUser> result = userService.getAll();

        assertEquals(1, result.size());
        verify(repository).findAll();
    }

    @Test
    void getById_shouldReturnUser() {
        AppUser user = new AppUser();
        user.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(user));

        AppUser result = userService.getById(1L);

        assertEquals(1L, result.getId());
        verify(repository).findById(1L);
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getById(1L));
    }

    @Test
    void delete_shouldRemoveUser() {
        AppUser user = new AppUser();
        user.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(user));

        userService.delete(1L);

        verify(repository).delete(user);
    }

    @Test
    void delete_shouldThrow_whenUserNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.delete(1L));

        verify(repository, never()).delete(any());
    }

    @Test
    void update_shouldModifyUsernameAndEmail() {
        Long userId = 1L;

        AppUser user = new AppUser();
        user.setId(userId);
        user.setUsername("oldUser");
        user.setEmail("old@email.com");

        UserDTO dto = new UserDTO();
        dto.setUsername("newUser");
        dto.setEmail("new@email.com");

        AppUser updated = new AppUser();
        updated.setUsername("newUser");
        updated.setEmail("new@email.com");

        when(repository.findById(userId)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(updated);

        AppUser result = userService.update(dto, userId);

        assertEquals("newUser", result.getUsername());
        assertEquals("new@email.com", result.getEmail());

        verify(repository).save(user);
    }

    @Test
    void update_shouldIgnoreBlankFields() {
        Long userId = 1L;

        AppUser user = new AppUser();
        user.setId(userId);
        user.setUsername("oldUser");
        user.setEmail("old@email.com");

        UserDTO dto = new UserDTO();
        dto.setUsername("   "); // blank
        dto.setEmail("");       // blank

        when(repository.findById(userId)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);

        userService.update(dto, userId);

        assertEquals("oldUser", user.getUsername());
        assertEquals("old@email.com", user.getEmail());

        verify(repository).save(user);
    }

    @Test
    void update_shouldIgnoreNullFields() {
        Long userId = 1L;

        AppUser user = new AppUser();
        user.setId(userId);
        user.setUsername("oldUser");
        user.setEmail("old@email.com");

        UserDTO dto = new UserDTO();
        dto.setUsername(null);
        dto.setEmail(null);

        when(repository.findById(userId)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);

        userService.update(dto, userId);

        assertEquals("oldUser", user.getUsername());
        assertEquals("old@email.com", user.getEmail());

        verify(repository).save(user);
    }

    @Test
    void update_shouldThrow_whenUserNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        UserDTO dto = new UserDTO();
        dto.setUsername("newUser");

        assertThrows(ResourceNotFoundException.class,
                () -> userService.update(dto, 1L));
    }

	    @Test
    void create_shouldThrow_whenRepeatedEmail() {

		when(repository.existsByEmail("test@email.com"))
			.thenReturn(false) // first call
			.thenReturn(true); // second call
			
        UserPassDTO request = new UserPassDTO();
        request.setUsername("testuser");
        request.setEmail("test@email.com");
        request.setPassword("plainPassword");

		UserPassDTO request2 = new UserPassDTO();
        request2.setUsername("testuser2");
        request2.setEmail("test@email.com");
        request2.setPassword("plainPassword");

        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");

        AppUser savedUser = AppUser.builder()
                .username("testuser")
                .email("test@email.com")
                .password("encodedPassword")
                .build();

        when(repository.save(any(AppUser.class))).thenReturn(savedUser);

        AppUser result = userService.create(request);

        assertEquals("testuser", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());

        verify(passwordEncoder).encode("plainPassword");
        verify(repository).save(any(AppUser.class));

		assertThrows(EmailInUseException.class,
			() -> userService.create(request2)
		);
    }
}
