package com.indramind.cybersec.secure_tasks_api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

import com.indramind.cybersec.secure_tasks_api.logging.CustomLogger;
import com.indramind.cybersec.secure_tasks_api.logging.impl.CustomLoggerFactory;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService; // loads user by username

    private static final CustomLogger log = CustomLoggerFactory.getLogger(JwtAuthenticationFilter.class);


    DefaultBearerTokenResolver resolver = new DefaultBearerTokenResolver();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String jwt = resolver.resolve(request); // remove "Bearer " and extract just the token

        if (jwt == null) {
            log.debug("No JWT token found: method={}, uri={}, ip={}",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr()); // This is debug because public endpoints would also trigger this (like /login)

            filterChain.doFilter(request, response);
            return;
        }


        final String username = jwtService.extractEmail(jwt); // If token is not valid username == null (exception caught in service layer and logged)

        if (!shouldAuthenticate(username)) {
            filterChain.doFilter(request, response);
            return;
        }

        UserDetails userDetails = loadUser(username, request);
        if (userDetails == null){
            filterChain.doFilter(request, response);
            return;
        }

        if (!checkTokenValid(jwt, userDetails, request)){
            filterChain.doFilter(request, response);
            return;
        }

        authenticateUser(userDetails, request);

        log.debug("JWT authentication success: username={}, ip={}", username, request.getRemoteAddr());

        filterChain.doFilter(request, response);
    }

    private boolean shouldAuthenticate(String username) {
        return username != null && SecurityContextHolder.getContext().getAuthentication() == null;
    }

    
    private UserDetails loadUser(String username, HttpServletRequest request){

        try {
            return userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            log.warn("JWT user not found: ip={}", request.getRemoteAddr());
            return null;
        }
    }

    private boolean checkTokenValid(String jwt, UserDetails userDetails, HttpServletRequest request){

        if (!jwtService.isTokenValid(jwt, userDetails)) {
            log.warn("JWT validation failed:  ip={}", request.getRemoteAddr());
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