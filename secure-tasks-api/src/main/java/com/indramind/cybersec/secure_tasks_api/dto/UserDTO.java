package com.indramind.cybersec.secure_tasks_api.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    @Length(min = 5, max = 30)
    private String username;

    @Email
    private String email;

    private Long id;
}