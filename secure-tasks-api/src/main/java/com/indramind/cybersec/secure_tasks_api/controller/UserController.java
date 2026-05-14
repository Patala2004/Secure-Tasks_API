package com.indramind.cybersec.secure_tasks_api.controller;

import com.indramind.cybersec.secure_tasks_api.dto.UserDTO;
import com.indramind.cybersec.secure_tasks_api.entity.AppUser;
import com.indramind.cybersec.secure_tasks_api.logging.CustomLogger;
import com.indramind.cybersec.secure_tasks_api.logging.impl.CustomLoggerFactory;
import com.indramind.cybersec.secure_tasks_api.mapper.UserMapper;
import com.indramind.cybersec.secure_tasks_api.security.utils.SecurityUtils;
import com.indramind.cybersec.secure_tasks_api.service.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;



@RestController
@Validated
@RequestMapping(value = "/api/users", produces = "application/json")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    private final UserMapper mapper;

    private static final CustomLogger log = CustomLoggerFactory.getLogger(ProjectController.class);

    @GetMapping("/{id}")
    public UserDTO getById(@PathVariable @Min(1) Long id) {
        log.info("Get user by id attempt started. User: {}", SecurityUtils.getLoggedUserId());
        UserDTO response =  mapper.toDto(service.getById(id));
        log.info("Get user by id attempt ended");
        return response;
    }

    @DeleteMapping(value = "/{id}")
    public void delete(@PathVariable @Min(1) Long id) {
        log.info("Delete user attempt started. User: {}", SecurityUtils.getLoggedUserId());
        service.delete(id);
        log.info("Delete user attempt ended");
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    public UserDTO update(@PathVariable @Min(1) Long id, @RequestBody @Valid UserDTO dto){
        log.info("Update user attempt started. User: {}", SecurityUtils.getLoggedUserId());
        UserDTO response =  mapper.toDto(service.update(dto, id));
        log.info("Update user attempt ended");
        return response;
    }
}