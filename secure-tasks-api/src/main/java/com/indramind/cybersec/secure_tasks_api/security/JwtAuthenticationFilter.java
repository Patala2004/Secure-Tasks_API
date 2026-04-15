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
        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No JWT token found: method={}, uri={}, ip={}",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr()); // This is debug because public endpoints would also trigger this (like /login)
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7); // remove "Bearer "
        username = jwtService.extractEmail(jwt); // If token is not valid username == null (exception caught in service layer and logged)

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails;
            try{
                userDetails = userDetailsService.loadUserByUsername(username);
            } catch (UsernameNotFoundException e) {
                log.warn("JWT user not found: email={}, ip={}", username, request.getRemoteAddr());
                filterChain.doFilter(request, response);
                return;
            }

            if (!jwtService.isTokenValid(jwt, userDetails)) {
                if (!jwtService.isTokenValid(jwt, userDetails)) {
                    log.warn("JWT validation failed: email={}, ip={}", username, request.getRemoteAddr());
                    filterChain.doFilter(request, response);
                    return;
                }
            }

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
            log.debug("JWT authentication success: username={}, ip={}", username, request.getRemoteAddr());
        }

        filterChain.doFilter(request, response);
    }
}