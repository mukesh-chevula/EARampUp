package com.example.project04mongodb_crud.controller;

import com.example.project04mongodb_crud.model.User;
import com.example.project04mongodb_crud.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean; // Deprecated in 3.4? No, typically standard.
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService service;

    @Autowired
    private ObjectMapper objectMapper;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User("696489df058b141ca4fcba93", "Daivik", "B", "Amazon", 5, "3456789012");
        user2 = new User("696489eb058b141ca4fcba94", "Mukesh", "CH", "EA", 5, "3456789012");
    }

    @Test
    void getAll_ShouldReturnListOfUsers() throws Exception {
        when(service.findAll()).thenReturn(Arrays.asList(user1, user2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("Daivik"))
                .andExpect(jsonPath("$[1].firstName").value("Mukesh"));
    }

    @Test
    void getUser_ShouldReturnUser_WhenFound() throws Exception {
        when(service.findById("696489df058b141ca4fcba93")).thenReturn(user1);

        mockMvc.perform(get("/users/696489df058b141ca4fcba93"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("696489df058b141ca4fcba93"))
                .andExpect(jsonPath("$.firstName").value("Daivik"));
    }

    @Test
    void createUser_ShouldReturnCreatedUser() throws Exception {
        when(service.save(any(User.class))).thenReturn(user1);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("696489df058b141ca4fcba93"))
                .andExpect(jsonPath("$.firstName").value("Daivik"));
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() throws Exception {
        when(service.update(eq("696489df058b141ca4fcba93"), any(User.class))).thenReturn(user1);

        mockMvc.perform(put("/users/696489df058b141ca4fcba93")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("696489df058b141ca4fcba93"))
                .andExpect(jsonPath("$.firstName").value("Daivik"));
    }

    @Test
    void deleteUser_ShouldReturnTrue_WhenDeleted() throws Exception {
        when(service.delete("696489df058b141ca4fcba93")).thenReturn(true);

        mockMvc.perform(delete("/users/696489df058b141ca4fcba93"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
