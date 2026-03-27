package com.appgestor.controller;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.appgestor.models.Category;
import com.appgestor.models.Movement;
import com.appgestor.models.User;
import com.appgestor.repository.MovementRepository;
import com.appgestor.repository.UserRepository;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private MovementRepository movementRepo;

    @PostMapping
    @Transactional
    public String transferir(@RequestParam Long originId,
                             @RequestParam Long destinyId,
                             @RequestParam BigDecimal amount) {

        User origin = userRepo.findById(originId).orElseThrow();
        User destiny = userRepo.findById(destinyId).orElseThrow();

        Movement expense = new Movement();
        expense.setUser(origin);
        expense.setCategory(Category.EXPENSE);
        expense.setAmount(amount);
        expense.setDescription("Transferencia enviada");
        expense.setDate(LocalDate.now());

        Movement income = new Movement();
        income.setUser(destiny);
        income.setCategory(Category.INCOME);
        income.setAmount(amount);
        income.setDescription("Transferencia recibida");
        income.setDate(LocalDate.now());

        movementRepo.save(expense);
        movementRepo.save(income);

        return "OK";
    }
}