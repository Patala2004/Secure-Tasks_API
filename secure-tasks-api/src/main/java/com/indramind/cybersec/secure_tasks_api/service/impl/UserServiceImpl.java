package com.indramind.cybersec.secure_tasks_api.service.impl;

import com.indramind.cybersec.secure_tasks_api.dto.UserDTO;
import com.indramind.cybersec.secure_tasks_api.dto.UserPassDTO;
import com.indramind.cybersec.secure_tasks_api.entity.AppUser;
import com.indramind.cybersec.secure_tasks_api.exceptions.EmailInUseException;
import com.indramind.cybersec.secure_tasks_api.exceptions.ResourceNotFoundException;
import com.indramind.cybersec.secure_tasks_api.repository.UserRepository;
import com.indramind.cybersec.secure_tasks_api.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AppUser create(@Valid UserPassDTO request) {
        if (repository.existsByEmail(request.getEmail())){
            throw new EmailInUseException("Email already in use");
        }
        AppUser user = AppUser.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // Hash password before storing
                .build();
        return repository.save(user);
    }

    @Override
    public List<AppUser> getAll() {
        return repository.findAll();
    }

    @Override
    public AppUser getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        AppUser user = getById(id);
        repository.delete(user);
    }

    @Override
    @Transactional
    @PreAuthorize("#id == authentication.principal.id")
    public AppUser update(@Valid UserDTO dto, Long id){
        if(!repository.existsById(id)) throw new ResourceNotFoundException("User with this id doesn't exist");
        AppUser user = getById(id);
        String newUsername = dto.getUsername();
        String newEmail = dto.getEmail();
        if(!user.getEmail().equals(newEmail) && repository.existsByEmail(newEmail)){
            throw new EmailInUseException("Email already in use");
        }
        if(newUsername != null && !newUsername.isBlank()) user.setUsername(newUsername);
        if(newEmail != null && !newEmail.isBlank()) user.setEmail(newEmail);
        return repository.save(user);
    }
}