package com.example.project03apis.service;

import com.example.project03apis.model.User;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.*;

@Service
public class FileUserService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final File file = new File("data/Users.json");
    private List<User> cache = new ArrayList<>();

    public FileUserService() throws IOException {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            mapper.writeValue(file, cache);
        } else {
            cache = mapper.readValue(file, new TypeReference<>() {});
        }
    }

    private void persist() throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, cache);
    }

    public List<User> findAll() {
        return cache;
    }

    public User findById(Long userId) {
        return cache.stream().filter(t -> t.getUserId().equals(userId)).findFirst().orElse(null);
    }

    public User save(User user) throws IOException {
        user.setUserId(null);
        long userId = cache.stream().mapToLong(User::getUserId).max().orElse(0) + 1;
        user.setUserId(userId);
        cache.add(user);
        persist();
        return user;
    }

    public User update(Long userId, User updated) throws IOException {
        User existing = findById(userId);
        if (existing == null) return null;
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setCompanyName(updated.getCompanyName());
        existing.setMobileNumber(updated.getMobileNumber());
        existing.setExperienceMonths(updated.getExperienceMonths());
        persist();
        return existing;
    }

    public boolean delete(Long userId) throws IOException {
        boolean removed = cache.removeIf(t -> t.getUserId().equals(userId));
        if (removed) persist();
        return removed;
    }
}
