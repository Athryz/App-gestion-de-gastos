package com.appgestor.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.appgestor.models.*;
import com.appgestor.repository.*;

@RestController
@RequestMapping("/api/movements")
@CrossOrigin(origins = "*")
public class MovementController {

    @Autowired private MovementRepository movementRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private NotificationRepository notificationRepo;

    // LISTAR: Filtra según el rol
    @GetMapping("/user/{userId}")
    public List<Movement> listByUser(@PathVariable Long userId) {
        User requester = userRepo.findById(userId).orElseThrow();
        String role = requester.getRole().toString();

        // ADMIN y SUPERADMIN ven todo
        if ("ADMIN".equals(role) || "SUPERADMIN".equals(role)) {
            return movementRepo.findAll();
        }
        // Usuario común solo ve lo suyo
        return movementRepo.findByUserId(userId);
    }

    // BORRAR: Control de acceso estricto
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, @RequestParam Long requesterId) {
        User requester = userRepo.findById(requesterId).orElseThrow();
        Movement mov = movementRepo.findById(id).orElseThrow();
        String role = requester.getRole().toString();

        // Lógica de permisos:
        // 1. Es el dueño del movimiento
        // 2. Es ADMIN o SUPERADMIN
        if (mov.getUser().getId().equals(requesterId) || "ADMIN".equals(role) || "SUPERADMIN".equals(role)) {
            movementRepo.delete(mov);
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.status(403).body("No tienes permisos para borrar este registro");
    }

    @PostMapping
    public ResponseEntity<Movement> create(@RequestBody Movement mov) {
        User user = userRepo.findById(mov.getUser().getId()).orElseThrow();
        mov.setUser(user);
        if (mov.getDate() == null) mov.setDate(LocalDate.now());
        return ResponseEntity.ok(movementRepo.save(mov));
    }
}