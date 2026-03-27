package com.appgestor.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service; // Recomendado
import org.springframework.transaction.annotation.Transactional;

import com.appgestor.models.Movement;
import com.appgestor.models.Notification; // CORREGIDO: era java.util, no java.utilities
import com.appgestor.models.Role;
import com.appgestor.models.User;
import com.appgestor.repository.MovementRepository;
import com.appgestor.repository.NotificationRepository;

@Service
public class MovementService {

    private final MovementRepository movementRepo;
    private final NotificationRepository notificationRepo;

    public MovementService(MovementRepository m, NotificationRepository n) {
        this.movementRepo = m;
        this.notificationRepo = n;
    }

    public Movement create(Movement mov, User user) {
        mov.setUser(user);
        mov.setDate(LocalDate.now());
        return movementRepo.save(mov);
    }

    public List<Movement> list(User user) {
        // Asegúrate de que User tenga el método getRol() y que Rol.BASIC exista
        if (user.getRole() == Role.BASIC) {
            return movementRepo.findByUserId(user.getId());
        }
        return movementRepo.findAll();
    }

    @Transactional
    public void delete(Long id, User admin) {
        Movement mov = movementRepo.findById(id).orElseThrow();

        if (admin.getRole() == Role.BASIC) {
            throw new RuntimeException("No autorizado");
        }

        movementRepo.delete(mov);

        Notification n = new Notification();
        n.setMessage("Movimiento eliminado por el administrador");
        n.setUser(mov.getUser());
        n.setDate(LocalDateTime.now());
        n.setRead(false);

        notificationRepo.save(n);
    }

    @Transactional
    public Movement edit(Long id, Movement nuevo, User user) {

        if (user.getRole() != Role.SUPERADMIN) {
            throw new RuntimeException("No autorizado");
        }

        Movement mov = movementRepo.findById(id).orElseThrow();

    
        mov.setDescription(nuevo.getDescription());
        mov.setAmount(nuevo.getAmount()); 
        mov.setCategory(nuevo.getCategory());

        Notification n = new Notification();
        n.setMessage("Movimiento modificado por el superadmin");
        n.setUser(mov.getUser());
        n.setDate(LocalDateTime.now());
        n.setRead(false);

        notificationRepo.save(n);

        return movementRepo.save(mov);
    }
}