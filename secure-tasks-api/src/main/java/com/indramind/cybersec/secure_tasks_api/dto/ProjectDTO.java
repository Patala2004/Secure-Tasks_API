package com.indramind.cybersec.secure_tasks_api.dto;

import org.hibernate.validator.constraints.Length;

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

	@NotNull
	@NotBlank
	@Length(min=5, max=50)
	private String name;
}
