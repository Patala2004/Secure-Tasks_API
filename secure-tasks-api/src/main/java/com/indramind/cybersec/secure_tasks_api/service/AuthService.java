package com.indramind.cybersec.secure_tasks_api.service;

import com.indramind.cybersec.secure_tasks_api.dto.RegisterRequest;
import com.indramind.cybersec.secure_tasks_api.entity.AppUser;

public interface AuthService {
	public String register(RegisterRequest dto);
	public String login(String email, String rawPassword);
	public AppUser getCurrentUser(String token);
}