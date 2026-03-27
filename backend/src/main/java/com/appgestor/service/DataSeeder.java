package com.appgestor.service;

import com.appgestor.models.Role;
import com.appgestor.models.User;
import com.appgestor.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(UserRepository repository) {
        return args -> {
           
            if (repository.findByEmail("superadmin@app.com").isEmpty()) {
                User superAdmin = new User();
                superAdmin.setName("Super Jefe");
                superAdmin.setEmail("superadmin@app.com");
                superAdmin.setPassword("1234"); 
                superAdmin.setRole(Role.SUPERADMIN);
                repository.save(superAdmin);
                System.out.println(">> SuperAdmin creado: superadmin@app.com / 1234");
            }

          
            if (repository.findByEmail("admin@app.com").isEmpty()) {
                User admin = new User();
                admin.setName("Administrador");
                admin.setEmail("admin@app.com");
                admin.setPassword("admin123");
                admin.setRole(Role.ADMIN);
                repository.save(admin);
                System.out.println(">> Admin creado: admin@app.com / admin123");
            }
        };
    }
}