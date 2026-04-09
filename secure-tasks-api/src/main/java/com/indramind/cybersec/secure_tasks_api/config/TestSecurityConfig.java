// package com.indramind.cybersec.secure_tasks_api.config;

// import org.springframework.boot.test.context.TestConfiguration;
// import org.springframework.context.annotation.*;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.web.SecurityFilterChain;

// @TestConfiguration
// public class TestSecurityConfig {

//     @Bean
// 	@Primary
//     public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//         http
//             .csrf(csrf -> csrf.disable())                // disable CSRF for tests
//             .authorizeHttpRequests(auth -> auth
//                 .anyRequest().permitAll()               // allow all requests
//             );
//         return http.build();
//     }
// }