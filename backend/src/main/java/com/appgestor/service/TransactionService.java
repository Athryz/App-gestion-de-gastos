package com.appgestor.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.appgestor.models.Category;
import com.appgestor.models.Movement;
import com.appgestor.models.User;
import com.appgestor.repository.MovementRepository;
import com.appgestor.repository.UserRepository;

@Service
public class TransactionService {

    private final UserRepository userRepo;
    private final MovementRepository movementRepo;

    public TransactionService(UserRepository u, MovementRepository m) {
        this.userRepo = u;
        this.movementRepo = m;
    }

    @Transactional
    public void transfer(Long sourceId, Long targetId, java.math.BigDecimal amount) {

        User source = userRepo.findById(sourceId).orElseThrow();
        User target = userRepo.findById(targetId).orElseThrow();

        Movement expense = new Movement();
        expense.setUser(source);
        expense.setCategory(Category.EXPENSE);
        expense.setAmount(amount);
        expense.setDescription("Transfer sent");
        expense.setDate(LocalDate.now());

        Movement income = new Movement();
        income.setUser(target);
        income.setCategory(Category.INCOME);
        income.setAmount(amount);
        income.setDescription("Transfer received");
        income.setDate(LocalDate.now());

        movementRepo.save(expense);
        movementRepo.save(income);
    }
}