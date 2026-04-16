package com.indramind.cybersec.secure_tasks_api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService; // loads user by username

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (isMissingOrInvalidHeader(authHeader)) {
            log.debug("No JWT token found: method={}, uri={}, ip={}",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr()); // This is debug because public endpoints would also trigger this (like /login)
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt;
        final String username;

        jwt = authHeader.substring(7); // remove "Bearer "
        username = jwtService.extractEmail(jwt); // If token is not valid username == null (exception caught in service layer and logged)

        if (!shouldAuthenticate(username)) {
            filterChain.doFilter(request, response);
            return;
        }

        UserDetails userDetails = loadUser(username, request, filterChain, response);
        if (userDetails == null){
            filterChain.doFilter(request, response);
            return;
        }

        if (!checkTokenValid(jwt, userDetails, request, filterChain, response)){
            filterChain.doFilter(request, response);
            return;
        }

        authenticateUser(userDetails, request);

        if (log.isDebugEnabled()) log.debug("JWT authentication success: username={}, ip={}", username, request.getRemoteAddr());

        filterChain.doFilter(request, response);
    }

    private boolean isMissingOrInvalidHeader(String authHeader) {
        return authHeader == null || !authHeader.startsWith("Bearer ");
    }

    private boolean shouldAuthenticate(String username) {
        return username != null && SecurityContextHolder.getContext().getAuthentication() == null;
    }

    
    private UserDetails loadUser(String username,
                                HttpServletRequest request,
                                FilterChain filterChain,
                                HttpServletResponse response)
            throws IOException, ServletException {

        try {
            return userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            if (log.isWarnEnabled()) {
                log.warn("JWT user not found: email={}, ip={}", username, request.getRemoteAddr());
            }
            return null;
        }
    }

    private boolean checkTokenValid(String jwt,
                                UserDetails userDetails,
                                HttpServletRequest request,
                                FilterChain filterChain,
                                HttpServletResponse response)
            throws IOException, ServletException {

        if (!jwtService.isTokenValid(jwt, userDetails)) {
            if (log.isWarnEnabled()) {
                log.warn("JWT validation failed: email={}, ip={}",
                        userDetails.getUsername(),
                        request.getRemoteAddr());
            }
            return false;
        }
        return true;
    }

    private void authenticateUser(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

        authToken.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

}