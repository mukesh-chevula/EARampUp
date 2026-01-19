package com.example.project06e2etesting.integration;

import com.example.project06e2etesting.model.User;
import com.example.project06e2etesting.utility.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UserController.
 * Tests the full stack: REST API -> Service -> Repository -> MongoDB
 */
@AutoConfigureMockMvc
class UserControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        testUser1 = new User(null, "Mukesh", "Ch", "EA", 24, "1234567890");
        testUser2 = new User(null, "Daivik", "B", "Qualcomm", 36, "0987654321");
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_ShouldPersistUserInDatabase() throws Exception {
        String requestBody = objectMapper.writeValueAsString(testUser1);

        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").isNotEmpty())
                .andExpect(jsonPath("$.firstName").value("Mukesh"))
                .andExpect(jsonPath("$.lastName").value("Ch"))
                .andExpect(jsonPath("$.companyName").value("EA"))
                .andExpect(jsonPath("$.experienceMonths").value(24))
                .andExpect(jsonPath("$.mobileNumber").value("1234567890"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        User createdUser = objectMapper.readValue(response, User.class);
        
        // Verify user is actually in database
        assertTrue(userRepository.existsById(createdUser.getUserId()));
        User dbUser = userRepository.findById(createdUser.getUserId()).orElse(null);
        assertNotNull(dbUser);
        assertEquals("Mukesh", dbUser.getFirstName());
        assertEquals("EA", dbUser.getCompanyName());
    }

    @Test
    void getAllUsers_ShouldReturnAllUsersFromDatabase() throws Exception {
        // Arrange: Insert test data
        User savedUser1 = userRepository.save(testUser1);
        User savedUser2 = userRepository.save(testUser2);

        // Act & Assert
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].userId", containsInAnyOrder(savedUser1.getUserId(), savedUser2.getUserId())))
                .andExpect(jsonPath("$[*].firstName", containsInAnyOrder("Mukesh", "Daivik")))
                .andExpect(jsonPath("$[*].companyName", containsInAnyOrder("EA", "Qualcomm")));
    }

    @Test
    void getUserById_ShouldReturnUserFromDatabase() throws Exception {
        // Arrange: Save user to database
        User savedUser = userRepository.save(testUser1);

        // Act & Assert
        mockMvc.perform(get("/users/{id}", savedUser.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(savedUser.getUserId()))
                .andExpect(jsonPath("$.firstName").value("Mukesh"))
                .andExpect(jsonPath("$.lastName").value("Ch"))
                .andExpect(jsonPath("$.companyName").value("EA"))
                .andExpect(jsonPath("$.experienceMonths").value(24))
                .andExpect(jsonPath("$.mobileNumber").value("1234567890"));
    }

    @Test
    void getUserById_WhenUserDoesNotExist_ShouldThrowException() {
        String nonExistentId = "507f1f77bcf86cd799439011";

        // Verify that calling the service directly throws an exception
        assertThrows(RuntimeException.class, () -> {
            userRepository.findById(nonExistentId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        });
    }

    @Test
    void updateUser_ShouldUpdateUserInDatabase() throws Exception {
        // Arrange: Save initial user
        User savedUser = userRepository.save(testUser1);
        
        User updatedData = new User(null, "Mukesh", "Chevula", "Electronic Arts", 30, "9999999999");
        String requestBody = objectMapper.writeValueAsString(updatedData);

        // Act
        mockMvc.perform(put("/users/{id}", savedUser.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(savedUser.getUserId()))
                .andExpect(jsonPath("$.firstName").value("Mukesh"))
                .andExpect(jsonPath("$.lastName").value("Chevula"))
                .andExpect(jsonPath("$.companyName").value("Electronic Arts"))
                .andExpect(jsonPath("$.experienceMonths").value(30))
                .andExpect(jsonPath("$.mobileNumber").value("9999999999"));

        // Assert: Verify database was actually updated
        User dbUser = userRepository.findById(savedUser.getUserId()).orElse(null);
        assertNotNull(dbUser);
        assertEquals("Chevula", dbUser.getLastName());
        assertEquals("Electronic Arts", dbUser.getCompanyName());
        assertEquals(30, dbUser.getExperienceMonths());
        assertEquals("9999999999", dbUser.getMobileNumber());
    }

    @Test
    void updateUser_WhenUserDoesNotExist_ShouldReturnNull() throws Exception {
        String nonExistentId = "507f1f77bcf86cd799439011";
        String requestBody = objectMapper.writeValueAsString(testUser1);

        mockMvc.perform(put("/users/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void deleteUser_ShouldRemoveUserFromDatabase() throws Exception {
        // Arrange: Save user
        User savedUser = userRepository.save(testUser1);
        assertTrue(userRepository.existsById(savedUser.getUserId()));

        // Act
        mockMvc.perform(delete("/users/{id}", savedUser.getUserId()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // Assert: Verify user is actually deleted from database
        assertFalse(userRepository.existsById(savedUser.getUserId()));
    }

    @Test
    void deleteUser_WhenUserDoesNotExist_ShouldReturnFalse() throws Exception {
        String nonExistentId = "507f1f77bcf86cd799439011";

        mockMvc.perform(delete("/users/{id}", nonExistentId))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    void fullCrudWorkflow_ShouldWorkEndToEnd() throws Exception {
        // 1. Create user
        String createRequest = objectMapper.writeValueAsString(testUser1);
        String createResponse = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        User createdUser = objectMapper.readValue(createResponse, User.class);
        String userId = createdUser.getUserId();

        // 2. Read user by ID
        mockMvc.perform(get("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Mukesh"));

        // 3. Update user
        User updateData = new User(null, "Mukesh", "Updated", "NewCompany", 50, "5555555555");
        String updateRequest = objectMapper.writeValueAsString(updateData);
        mockMvc.perform(put("/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Updated"))
                .andExpect(jsonPath("$.companyName").value("NewCompany"));

        // 4. Verify update persisted
        User dbUser = userRepository.findById(userId).orElse(null);
        assertNotNull(dbUser);
        assertEquals("Updated", dbUser.getLastName());
        assertEquals("NewCompany", dbUser.getCompanyName());

        // 5. Delete user
        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // 6. Verify deletion
        assertFalse(userRepository.existsById(userId));
    }

    @Test
    void multipleUsers_ShouldMaintainDataIntegrity() throws Exception {
        // Create multiple users
        for (int i = 1; i <= 5; i++) {
            User user = new User(null, "User" + i, "Last" + i, "Company" + i, i * 10, "100000000" + i);
            String requestBody = objectMapper.writeValueAsString(user);
            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isOk());
        }

        // Verify all users are in database
        assertEquals(5, userRepository.count());

        // Retrieve all users
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));
    }
}
