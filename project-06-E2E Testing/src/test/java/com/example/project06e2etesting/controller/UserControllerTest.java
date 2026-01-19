package com.example.project06e2etesting.controller;

import com.example.project06e2etesting.model.User;
import com.example.project06e2etesting.service.UserService;
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
        user1 = new User("1", "Mukesh", "Ch", "EA", 24, "1234567890");
        user2 = new User("2", "Daivik", "B", "Qualcomm", 36, "0987654321");
    }

    @Test
    void getAll_ShouldReturnListOfUsers() throws Exception {
        when(service.findAll()).thenReturn(Arrays.asList(user1, user2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].firstName").value("Mukesh"))
                .andExpect(jsonPath("$[1].firstName").value("Daivik"));
    }

    @Test
    void getUser_ShouldReturnUser_WhenFound() throws Exception {
        when(service.findById("1")).thenReturn(user1);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("1"))
                .andExpect(jsonPath("$.firstName").value("Mukesh"));
    }

    @Test
    void createUser_ShouldReturnCreatedUser() throws Exception {
        when(service.save(any(User.class))).thenReturn(user1);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("1"))
                .andExpect(jsonPath("$.firstName").value("Mukesh"));
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() throws Exception {
        when(service.update(eq("1"), any(User.class))).thenReturn(user1);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("1"))
                .andExpect(jsonPath("$.firstName").value("Mukesh"));
    }

    @Test
    void deleteUser_ShouldReturnTrue_WhenDeleted() throws Exception {
        when(service.delete("1")).thenReturn(true);

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}
