package com.indramind.cybersec.secure_tasks_api.controller;

import com.indramind.cybersec.secure_tasks_api.dto.UserDTO;
import com.indramind.cybersec.secure_tasks_api.entity.AppUser;
import com.indramind.cybersec.secure_tasks_api.mapper.UserMapper;
import com.indramind.cybersec.secure_tasks_api.service.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Validated
@RequestMapping(value = "/api/users", produces = "application/json")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    private final UserMapper mapper;

    @GetMapping
    public List<AppUser> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public AppUser getById(@PathVariable @Min(1) Long id) {
        return service.getById(id);
    }

    @DeleteMapping(value = "/{id}")
    public void delete(@PathVariable @Min(1) Long id) {
        service.delete(id);
    }

    @PutMapping(value = "/{id}", consumes = "application/json")
    public UserDTO update(@PathVariable @Min(1) Long id, @RequestBody @Valid UserDTO dto){
        return mapper.toDto(service.update(dto, id));
    }
}