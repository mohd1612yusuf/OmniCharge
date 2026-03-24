package com.omnicharge.userservice.controller;

import com.omnicharge.userservice.model.User;
import com.omnicharge.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    UserRepository userRepository;

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserProfile(@PathVariable("id") Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }
        return ResponseEntity.notFound().build();
    }
}
