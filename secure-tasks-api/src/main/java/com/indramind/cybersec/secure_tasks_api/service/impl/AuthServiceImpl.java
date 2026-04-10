package com.indramind.cybersec.secure_tasks_api.service.impl;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.indramind.cybersec.secure_tasks_api.dto.RegisterRequest;
import com.indramind.cybersec.secure_tasks_api.entity.AppUser;
import com.indramind.cybersec.secure_tasks_api.exceptions.EmailInUseException;
import com.indramind.cybersec.secure_tasks_api.exceptions.ResourceNotFoundException;
import com.indramind.cybersec.secure_tasks_api.repository.UserRepository;
import com.indramind.cybersec.secure_tasks_api.security.JwtService;
import com.indramind.cybersec.secure_tasks_api.security.UserDetailsServiceImpl;
import com.indramind.cybersec.secure_tasks_api.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;
	private final UserDetailsServiceImpl userDetailsService;
	
	public String register(RegisterRequest dto){
		// Check if mail already exists
		if(userRepository.findByEmail(dto.getEmail()).isPresent()){
			throw new EmailInUseException("Email already in use");
		}

		// Create new user with hashed password
		AppUser user = new AppUser();
		user.setEmail(dto.getEmail());
		user.setUsername(dto.getUsername());
		user.setPassword(passwordEncoder.encode(dto.getPassword()));

		userRepository.save(user);

		UserDetails userDetail = userDetailsService.loadUserByUsername(user.getEmail());

		// Generate JWT and give it to user
		return jwtService.generateAccessToken(userDetail);
	}


	public String login(String email, String rawPassword){

		Authentication authentication = authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(email, rawPassword)
		); // Check email user existence + password hash check while protecting against enumeration attacks and timing checks
		// Throws BadCredentialsException (handeled by springsecurity automatically)

		UserDetails userDetails = (UserDetails) authentication.getPrincipal();

		// Generate JWT token
		return jwtService.generateAccessToken(userDetails);
	}

	public AppUser getCurrentUser(String token){
		String email = jwtService.extractEmail(token);

		return userRepository.findByEmail(email)
			.orElseThrow(() -> new ResourceNotFoundException("User for this token doesn't exist"));
	}
	
}