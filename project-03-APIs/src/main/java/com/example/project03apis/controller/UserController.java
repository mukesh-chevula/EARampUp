package com.example.project03apis.controller;

import com.example.project03apis.model.User;
import com.example.project03apis.service.FileUserService;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final FileUserService service;

    public UserController(FileUserService service){
        this.service=service;
    }

    @GetMapping
    public List<User> getAll(){
        return service.findAll();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id){
        return service.findById(id);
    }

    @PostMapping
    public User createUser(@RequestBody User user) throws IOException {
        return service.save(user);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id,@RequestBody User user) throws IOException {
        return service.update(id, user);
    }

    @DeleteMapping("/{id}")
    public Boolean deleteUser(@PathVariable Long id) throws IOException {
        return service.delete(id);
    }
}
