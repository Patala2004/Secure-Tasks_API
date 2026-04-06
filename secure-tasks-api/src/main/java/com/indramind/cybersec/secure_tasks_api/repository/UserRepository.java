package com.indramind.cybersec.secure_tasks_api.repository;


import com.indramind.cybersec.secure_tasks_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}