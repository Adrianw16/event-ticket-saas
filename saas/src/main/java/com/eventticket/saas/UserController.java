package com.eventticket.saas;

import  org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(){
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping
    public  ResponseEntity<User> createUser(@RequestBody User user){
        User saved = userRepository.save(user);
        return ResponseEntity.status(201).body(saved);
    }
}
