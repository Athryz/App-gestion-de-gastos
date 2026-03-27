package com.appgestor.controller;

import com.appgestor.models.Role;
import com.appgestor.models.User;
import com.appgestor.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")

@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500"})
public class AuthController {

    @Autowired
    private UserRepository userRepo;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
      
        if (userRepo.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("Error: El correo electrónico ya está registrado.");
        }

        user.setRole(Role.BASIC); 

        try {
            User savedUser = userRepo.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al crear la cuenta: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginData) {
     
        Optional<User> userOpt = userRepo.findByEmail(loginData.getEmail());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            if (user.getPassword().equals(loginData.getPassword())) {
           
                return ResponseEntity.ok(user);
            }
        }

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body("Credenciales inválidas. Revisa tu email y contraseña.");
    }
}