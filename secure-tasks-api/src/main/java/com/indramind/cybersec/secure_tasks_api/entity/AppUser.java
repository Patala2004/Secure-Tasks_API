package com.indramind.cybersec.secure_tasks_api.entity;

import org.hibernate.validator.constraints.Length;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Length(min=5, max=30)
	@Pattern(
        regexp = "^[a-zA-Z0-9._-]{5,30}$",
        message = "Username may contain only letters, numbers, dots (.), underscores (_) and hyphens (-)"
    )
	private String username;

	@Column(unique = true, nullable = false)
	@Email
	private String email;

	@Column(nullable = false)
	@Length(min=7)
	private String password;

}
