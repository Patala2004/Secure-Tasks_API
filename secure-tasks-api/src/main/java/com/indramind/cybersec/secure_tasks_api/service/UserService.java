package com.indramind.cybersec.secure_tasks_api.service;

import com.indramind.cybersec.secure_tasks_api.entity.User;
import com.indramind.cybersec.secure_tasks_api.dto.UserDTO;

import java.util.List;

public interface UserService {
    User create(UserDTO request);
    List<User> getAll();
    User getById(Long id);
    void delete(Long id);
}