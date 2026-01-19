package com.example.project06e2etesting.controller;

import com.example.project06e2etesting.model.User;
import com.example.project06e2etesting.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service){
        this.service=service;
    }

    @GetMapping
    public List<User> getAll(){
        return service.findAll();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable String id){
        return service.findById(id);
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return service.save(user);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable String id, @RequestBody User user) {
        return service.update(id, user);
    }

    @DeleteMapping("/{id}")
    public Boolean deleteUser(@PathVariable String id) {
        return service.delete(id);
    }
}
