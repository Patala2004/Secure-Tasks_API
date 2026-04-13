package com.indramind.cybersec.secure_tasks_api.service;

import com.indramind.cybersec.secure_tasks_api.entity.AppUser;
import com.indramind.cybersec.secure_tasks_api.dto.UserDTO;
import com.indramind.cybersec.secure_tasks_api.dto.UserPassDTO;

import java.util.List;

public interface UserService {
    AppUser create(UserPassDTO request);
    List<AppUser> getAll();
    AppUser getById(Long id);
    void delete(Long id);
    AppUser update(UserDTO user, Long id);
}