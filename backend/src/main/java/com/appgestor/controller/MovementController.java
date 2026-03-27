package com.appgestor.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.appgestor.models.*;
import com.appgestor.service.MovementService;

@RestController
@RequestMapping("/api/movements")

@CrossOrigin(origins = "http://127.0.0.1:5500", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class MovementController {

    @Autowired 
    private MovementService movementService;

    
    @GetMapping("/user/{userId}")
    public List<Movement> list(@PathVariable Long userId) {
        return movementService.listByUser(userId);
    }

    
    @PostMapping("/{userId}")
    public ResponseEntity<Movement> create(@RequestBody Movement mov, @PathVariable Long userId) {
        return ResponseEntity.ok(movementService.create(mov, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Movement> update(@PathVariable Long id, @RequestBody Movement mov, @RequestParam Long requesterId) {
        return ResponseEntity.ok(movementService.edit(id, mov, requesterId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, @RequestParam Long requesterId) {
        movementService.delete(id, requesterId);
        return ResponseEntity.ok().build();
    }
}