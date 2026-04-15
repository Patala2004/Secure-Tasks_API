package com.indramind.cybersec.secure_tasks_api.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    @Size(min=5, max=30, message = "Username must be 5-30 characters long")
    @Pattern(
        regexp = "^[a-zA-Z0-9._-]{5,30}$",
        message = "Username may contain only letters, numbers, dots (.), underscores (_) and hyphens (-)"
    )
    private String username;

    @Email
    private String email;

    private Long id;
}