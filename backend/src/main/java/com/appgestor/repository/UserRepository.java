package com.appgestor.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.appgestor.models.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
