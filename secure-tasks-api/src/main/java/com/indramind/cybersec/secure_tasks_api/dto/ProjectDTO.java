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
public class ProjectDTO {
	private Long id;

	@NotBlank(message = "Project name is required")
	@Size(min=5, max=50, message = "Project name must be 5-50 characters long")
	@Pattern(
		regexp = "^[a-zA-Z0-9 ._-]{5,50}$",
		message = "Project name may only contain letters, numbers, spaces, dots (.), underscores (_) and hyphens (-)"
	)
	private String name;
}
