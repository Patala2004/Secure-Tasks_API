package com.indramind.cybersec.secure_tasks_api.security;

import com.indramind.cybersec.secure_tasks_api.entity.AppUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
public class UserDetailsImpl implements UserDetails {

    private final String email;
    private final String password;
    private final Long id;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(AppUser user, Collection<? extends GrantedAuthority> authorities) {
        this.email = user.getEmail();       // unique identifier
        this.password = user.getPassword();
        this.id = user.getId();
        this.authorities = authorities;
    }

    @Override
    public String getUsername() {
        return email; // Spring Security treats this as the principal
    }

    public Long getId() {
        return id; 
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities; // Spring Security treats this as the principal
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}