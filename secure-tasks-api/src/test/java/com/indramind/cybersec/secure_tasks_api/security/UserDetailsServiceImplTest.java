package com.indramind.cybersec.secure_tasks_api.security;

import com.indramind.cybersec.secure_tasks_api.entity.AppUser;
import com.indramind.cybersec.secure_tasks_api.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    // ---------------- LOAD USER BY USERNAME ----------------

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        String email = "test@email.com";

        AppUser user = new AppUser();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername(email);

        assertNotNull(result);
        assertEquals(email, result.getUsername());

        verify(userRepository).findByEmail(email);
    }

    @Test
    void loadUserByUsername_shouldThrow_whenUserNotFound() {
        String email = "test@email.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(email));

        verify(userRepository).findByEmail(email);
    }

    // ---------------- GET FROM USER ----------------

    @Test
    void getFromUser_shouldReturnUserDetails() {
        AppUser user = new AppUser();
        user.setEmail("test@email.com");

        UserDetails result = userDetailsService.getFromUser(user);

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getUsername());
    }
}