package com.indramind.cybersec.secure_tasks_api.security;

import com.indramind.cybersec.secure_tasks_api.entity.AppUser;
import com.indramind.cybersec.secure_tasks_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // You can assign authorities (roles) here if you have a Role entity
        return new UserDetailsImpl(user, null);
    }

    public UserDetails getFromUser(AppUser user) {
        return new UserDetailsImpl(user, null);
    }
}