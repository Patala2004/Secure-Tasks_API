package com.indramind.cybersec.secure_tasks_api.entity;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.constraints.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
    name = "projects",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "name"}) // proj. name is unique per user
    }
)
public class Project {
	@Id
	@GeneratedValue
	private Long id;

	@ManyToOne
	private AppUser owner;

	@ManyToMany
	@Builder.Default
	private Set<AppUser> collaborators = new HashSet<>();

	@NotNull
	@NotBlank
	@Length(min=5, max=50)
	private String name;

	public void addCollaborator(AppUser user) {
		if (!collaborators.contains(user)) {
			collaborators.add(user);
		}
	}

	public void removeCollaborator(AppUser user) {
		if (collaborators != null) {
			collaborators.remove(user);
		}
	}
}
