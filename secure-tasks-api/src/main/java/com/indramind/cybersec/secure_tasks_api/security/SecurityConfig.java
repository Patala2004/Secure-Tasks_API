package com.indramind.cybersec.secure_tasks_api.security;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    @SuppressWarnings("java:S4502") // Stateless JWT-based API, no session cookies => CSRF not applicable
    public SecurityFilterChain securityFilterChain(HttpSecurity http){
        http
            .csrf(AbstractHttpConfigurer::disable) // csrf -> csrf.diable Because we use stateless jwt and dont have session cookies
			.formLogin(form -> form.disable()) // disable default /login page
			.httpBasic(AbstractHttpConfigurer::disable) // httpBasic -> httpBasic.disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    if (log.isWarnEnabled()) log.warn("Authentication failed: method={}, uri={}, ip={}, errorType={}, correlationId={}",
                        request.getMethod(),
                        request.getRequestURI(),
                        request.getRemoteAddr(),
                        authException.getClass().getSimpleName(),
                        MDC.get(CorrelationIdFilter.CORRELATION_KEY));
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    if (log.isWarnEnabled()) log.warn("Access denied: method={}, uri={}, ip={}, correlationId={}",
                        request.getMethod(),
                        request.getRequestURI(),
                        request.getRemoteAddr(),
                        MDC.get(CorrelationIdFilter.CORRELATION_KEY));
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                })
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config){
        return config.getAuthenticationManager();
    }
}