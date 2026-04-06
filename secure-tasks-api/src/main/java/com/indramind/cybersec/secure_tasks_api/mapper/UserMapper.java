package com.indramind.cybersec.secure_tasks_api.mapper;

import org.springframework.stereotype.Component;

import com.indramind.cybersec.secure_tasks_api.dto.UserDTO;
import com.indramind.cybersec.secure_tasks_api.entity.AppUser;

@Component
public class UserMapper {
	
    public UserDTO toDto(AppUser user) {
        if (user == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        return dto;
    }

    public AppUser toEntity(UserDTO dto) {
        if (dto == null) {
            return null;
        }

        AppUser user = new AppUser();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        // Do NOT set password here — use service for that
        return user;
    }
}