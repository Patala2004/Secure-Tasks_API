package com.indramind.cybersec.secure_tasks_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @Email
    private String email;

	@NotBlank(message = "Password is required")
	@Size(min = 9, max = 200, message = "Password must be 9-200 characters long")
	@Pattern(
        regexp = "^[\\p{Print}]+$", // Only allow printable characters (dont allow control chars)
        message = "Password may contain only printable characters"
    ) 
	private String password;
}