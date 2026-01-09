package com.example.project04mongodb_crud.service;

import com.example.project04mongodb_crud.model.User;
import com.example.project04mongodb_crud.utility.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public List<User> findAll() {
        return repo.findAll();
    }

    public User findById(String id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User save(User user) {
        return repo.save(user);
    }

    public User update(String id, User updated) {
        User existing = repo.findById(id).orElse(null);
        if (existing == null) return null;

        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setCompanyName(updated.getCompanyName());
        existing.setMobileNumber(updated.getMobileNumber());
        existing.setExperienceMonths(updated.getExperienceMonths());

        return repo.save(existing);
    }

    public Boolean delete(String id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return true;
        }
        return false;
    }
}
