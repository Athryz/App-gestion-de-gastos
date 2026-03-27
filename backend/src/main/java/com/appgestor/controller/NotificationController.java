package com.appgestor.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.appgestor.models.User;
import com.appgestor.repository.NotificationRepository;
import com.appgestor.repository.UserRepository;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificationController {

    @Autowired
    private NotificationRepository repo;

    @Autowired
    private UserRepository userRepo;

    private User getSignedInUser() {
        return userRepo.findById(1L).orElseThrow();
    }

    @GetMapping
    public List<?> list() {
        return repo.findByUserId(getSignedInUser().getId());
    }
}