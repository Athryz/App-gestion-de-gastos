package com.appgestor.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.appgestor.models.*;
import com.appgestor.repository.*;

@Service
public class TransactionService {

    private final UserRepository userRepo;
    private final MovementRepository movementRepo;
    private final NotificationRepository notificationRepo;

    public TransactionService(UserRepository u, MovementRepository m, NotificationRepository n) {
        this.userRepo = u;
        this.movementRepo = m;
        this.notificationRepo = n;
    }

    @Transactional
    public void transfer(Long sourceId, Long targetId, BigDecimal amount) {
        User source = userRepo.findById(sourceId).orElseThrow();
        User target = userRepo.findById(targetId).orElseThrow();

        // Gasto para el origen
        Movement expense = new Movement();
        expense.setUser(source);
        expense.setCategory(Category.EXPENSE);
        expense.setAmount(amount);
        expense.setDescription("Transferencia enviada a " + target.getName());
        expense.setDate(LocalDate.now());

        // Ingreso para el destino
        Movement income = new Movement();
        income.setUser(target);
        income.setCategory(Category.INCOME);
        income.setAmount(amount);
        income.setDescription("Transferencia recibida de " + source.getName());
        income.setDate(LocalDate.now());

        movementRepo.save(expense);
        movementRepo.save(income);

        // Notificar al destino
        Notification n = new Notification();
        n.setUser(target);
        n.setMessage("Has recibido una transferencia de " + amount + "€ de " + source.getName());
        n.setDate(LocalDateTime.now());
        n.setRead(false);
        notificationRepo.save(n);
    }
}