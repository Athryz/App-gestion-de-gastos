package com.appgestor.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.appgestor.models.Movement;
import com.appgestor.models.Notification;
import com.appgestor.models.Role;
import com.appgestor.models.User;
import com.appgestor.repository.MovementRepository;
import com.appgestor.repository.NotificationRepository;
import com.appgestor.repository.UserRepository;

@Service
public class MovementService {

    private final MovementRepository movementRepo;
    private final NotificationRepository notificationRepo;
    private final UserRepository userRepo;

    public MovementService(MovementRepository m, NotificationRepository n, UserRepository u) {
        this.movementRepo = m;
        this.notificationRepo = n;
        this.userRepo = u;
    }

    public List<Movement> listByUser(Long userId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (user.getRole() == Role.BASIC) {
            return movementRepo.findByUserId(userId);
        }
    
        return movementRepo.findAll();
    }

    @Transactional
    public Movement create(Movement mov, Long userId) {
        User user = userRepo.findById(userId).orElseThrow();
        mov.setUser(user);
        if (mov.getDate() == null) mov.setDate(LocalDate.now());
        return movementRepo.save(mov);
    }

    @Transactional
    public void delete(Long id, Long requesterId) {
        User requester = userRepo.findById(requesterId).orElseThrow();
        Movement mov = movementRepo.findById(id).orElseThrow();

    
        if (requester.getRole() == Role.BASIC) {
            throw new RuntimeException("No autorizado para borrar");
        }

        movementRepo.delete(mov);

  
        createNotification(mov.getUser(), "Un administrador ha eliminado tu movimiento: " + mov.getDescription());
    }

    @Transactional
    public Movement edit(Long id, Movement nuevo, Long requesterId) {
        User requester = userRepo.findById(requesterId).orElseThrow();
        Movement mov = movementRepo.findById(id).orElseThrow();

       
        if (requester.getRole() == Role.BASIC) {
            throw new RuntimeException("No autorizado para editar");
        }

        mov.setDescription(nuevo.getDescription());
        mov.setAmount(nuevo.getAmount());
        mov.setCategory(nuevo.getCategory());
       

        Movement actualizado = movementRepo.save(mov);
        createNotification(mov.getUser(), "Un administrador ha modificado tu movimiento: " + mov.getDescription());
        
        return actualizado;
    }

    private void createNotification(User user, String message) {
        Notification n = new Notification();
        n.setMessage(message);
        n.setUser(user);
        n.setDate(LocalDateTime.now());
        n.setRead(false);
        notificationRepo.save(n);
    }
}