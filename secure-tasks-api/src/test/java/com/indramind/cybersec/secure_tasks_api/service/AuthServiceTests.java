package com.indramind.cybersec.secure_tasks_api.service;

import com.indramind.cybersec.secure_tasks_api.dto.RegisterRequest;
import com.indramind.cybersec.secure_tasks_api.entity.AppUser;
import com.indramind.cybersec.secure_tasks_api.exceptions.EmailInUseException;
import com.indramind.cybersec.secure_tasks_api.exceptions.ResourceNotFoundException;
import com.indramind.cybersec.secure_tasks_api.repository.UserRepository;
import com.indramind.cybersec.secure_tasks_api.security.JwtService;
import com.indramind.cybersec.secure_tasks_api.security.UserDetailsServiceImpl;
import com.indramind.cybersec.secure_tasks_api.service.impl.AuthServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @InjectMocks
    private AuthServiceImpl authService;

    // ---------------- REGISTER ----------------

    @Test
    void register_shouldCreateUserAndReturnToken() {
        RegisterRequest dto = new RegisterRequest();
        dto.setEmail("test@email.com");
        dto.setUsername("testuser");
        dto.setPassword("password");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        UserDetails userDetails = mock(UserDetails.class);

        when(userDetailsService.loadUserByUsername(dto.getEmail())).thenReturn(userDetails);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("jwt-token");

        String result = authService.register(dto);

        assertEquals("jwt-token", result);

        verify(userRepository).save(any(AppUser.class));
        verify(passwordEncoder).encode("password");
        verify(jwtService).generateAccessToken(userDetails);
    }

    @Test
    void register_shouldThrow_whenEmailAlreadyExists() {
        RegisterRequest dto = new RegisterRequest();
        dto.setEmail("test@email.com");

        when(userRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.of(new AppUser()));

        assertThrows(EmailInUseException.class,
                () -> authService.register(dto));

        verify(userRepository, never()).save(any());
    }

    // ---------------- LOGIN ----------------

    @Test
    void login_shouldAuthenticateAndReturnToken() {
        String email = "test@email.com";
        String password = "password";

        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("jwt-token");

        String result = authService.login(email, password);

        assertEquals("jwt-token", result);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateAccessToken(userDetails);
    }

    @Test
    void login_shouldThrow_whenAuthenticationFails() {
        String email = "test@email.com";
        String password = "wrongPassword";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        assertThrows(RuntimeException.class,
                () -> authService.login(email, password));
    }

    // ---------------- GET CURRENT USER ----------------

    @Test
    void getCurrentUser_shouldReturnUser() {
        String token = "valid-token";
        String email = "test@email.com";

        AppUser user = new AppUser();
        user.setEmail(email);

        when(jwtService.extractEmail(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        AppUser result = authService.getCurrentUser(token);

        assertEquals(email, result.getEmail());
        verify(jwtService).extractEmail(token);
    }

    @Test
    void getCurrentUser_shouldThrow_whenUserNotFound() {
        String token = "valid-token";
        String email = "test@email.com";

        when(jwtService.extractEmail(token)).thenReturn(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> authService.getCurrentUser(token));
    }
}