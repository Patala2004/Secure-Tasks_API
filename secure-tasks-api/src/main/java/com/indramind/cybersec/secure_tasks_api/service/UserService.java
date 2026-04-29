package com.indramind.cybersec.secure_tasks_api.service;

import com.indramind.cybersec.secure_tasks_api.entity.AppUser;

import jakarta.validation.Valid;

import com.indramind.cybersec.secure_tasks_api.dto.UserDTO;
import com.indramind.cybersec.secure_tasks_api.dto.UserPassDTO;

import java.util.List;

public interface UserService {
    AppUser create(@Valid UserPassDTO request);
    List<AppUser> getAll();
    AppUser getById(Long id);
    void delete(Long id);
    AppUser update(@Valid UserDTO user, Long id);
}