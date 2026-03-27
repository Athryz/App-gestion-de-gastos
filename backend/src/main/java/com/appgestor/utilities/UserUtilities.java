package com.appgestor.utilities;

import org.springframework.stereotype.Component;

import com.appgestor.models.User;
import com.appgestor.repository.UserRepository;

@Component
public class UserUtilities {

    private final UserRepository repo;

    public UserUtilities(UserRepository repo) {
        this.repo = repo;
    }

    public User getLoggedUser() {
        return repo.findById(1L).orElseThrow();
    }
}