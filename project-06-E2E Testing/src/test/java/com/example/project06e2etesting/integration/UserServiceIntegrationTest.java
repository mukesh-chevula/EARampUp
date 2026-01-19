package com.example.project06e2etesting.integration;

import com.example.project06e2etesting.model.User;
import com.example.project06e2etesting.service.UserService;
import com.example.project06e2etesting.utility.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserService.
 * Tests the service layer with actual MongoDB database operations.
 */
class UserServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

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
    void save_ShouldPersistUserInDatabase() {
        User savedUser = userService.save(testUser1);

        assertNotNull(savedUser.getUserId());
        assertEquals("Mukesh", savedUser.getFirstName());
        assertEquals("EA", savedUser.getCompanyName());

        // Verify in database
        assertTrue(userRepository.existsById(savedUser.getUserId()));
    }

    @Test
    void findAll_ShouldReturnAllUsersFromDatabase() {
        userRepository.save(testUser1);
        userRepository.save(testUser2);

        List<User> users = userService.findAll();

        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getFirstName().equals("Mukesh")));
        assertTrue(users.stream().anyMatch(u -> u.getFirstName().equals("Daivik")));
    }

    @Test
    void findAll_WhenNoUsers_ShouldReturnEmptyList() {
        List<User> users = userService.findAll();
        assertTrue(users.isEmpty());
    }

    @Test
    void findById_ShouldReturnUserFromDatabase() {
        User savedUser = userRepository.save(testUser1);

        User foundUser = userService.findById(savedUser.getUserId());

        assertNotNull(foundUser);
        assertEquals(savedUser.getUserId(), foundUser.getUserId());
        assertEquals("Mukesh", foundUser.getFirstName());
        assertEquals("EA", foundUser.getCompanyName());
    }

    @Test
    void findById_WhenUserDoesNotExist_ShouldThrowException() {
        String nonExistentId = "507f1f77bcf86cd799439011";

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.findById(nonExistentId);
        });

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void update_ShouldModifyExistingUserInDatabase() {
        User savedUser = userRepository.save(testUser1);
        String userId = savedUser.getUserId();

        User updateData = new User(null, "Mukesh", "Chevula", "Electronic Arts", 30, "9999999999");
        User updatedUser = userService.update(userId, updateData);

        assertNotNull(updatedUser);
        assertEquals(userId, updatedUser.getUserId());
        assertEquals("Mukesh", updatedUser.getFirstName());
        assertEquals("Chevula", updatedUser.getLastName());
        assertEquals("Electronic Arts", updatedUser.getCompanyName());
        assertEquals(30, updatedUser.getExperienceMonths());
        assertEquals("9999999999", updatedUser.getMobileNumber());

        // Verify changes persisted in database
        User dbUser = userRepository.findById(userId).orElse(null);
        assertNotNull(dbUser);
        assertEquals("Chevula", dbUser.getLastName());
        assertEquals("Electronic Arts", dbUser.getCompanyName());
        assertEquals(30, dbUser.getExperienceMonths());
    }

    @Test
    void update_WhenUserDoesNotExist_ShouldReturnNull() {
        String nonExistentId = "507f1f77bcf86cd799439011";
        User updateData = new User(null, "Test", "User", "TestCo", 10, "0000000000");

        User result = userService.update(nonExistentId, updateData);

        assertNull(result);
    }

    @Test
    void update_ShouldPreserveUserId() {
        User savedUser = userRepository.save(testUser1);
        String originalUserId = savedUser.getUserId();

        User updateData = new User(null, "NewName", "NewLast", "NewCompany", 99, "1111111111");
        User updatedUser = userService.update(originalUserId, updateData);

        assertEquals(originalUserId, updatedUser.getUserId());
    }

    @Test
    void delete_ShouldRemoveUserFromDatabase() {
        User savedUser = userRepository.save(testUser1);
        String userId = savedUser.getUserId();
        assertTrue(userRepository.existsById(userId));

        Boolean result = userService.delete(userId);

        assertTrue(result);
        assertFalse(userRepository.existsById(userId));
    }

    @Test
    void delete_WhenUserDoesNotExist_ShouldReturnFalse() {
        String nonExistentId = "507f1f77bcf86cd799439011";

        Boolean result = userService.delete(nonExistentId);

        assertFalse(result);
    }

    @Test
    void concurrentOperations_ShouldMaintainDataConsistency() {
        // Create initial user
        User user1 = userService.save(testUser1);
        String userId = user1.getUserId();

        // Retrieve user
        User retrieved = userService.findById(userId);
        assertEquals("Mukesh", retrieved.getFirstName());

        // Update user
        User updateData = new User(null, "Mukesh", "Updated", "NewCo", 50, "5555555555");
        User updated = userService.update(userId, updateData);
        assertEquals("Updated", updated.getLastName());

        // Verify update
        User verified = userService.findById(userId);
        assertEquals("Updated", verified.getLastName());
        assertEquals("NewCo", verified.getCompanyName());

        // Delete user
        Boolean deleted = userService.delete(userId);
        assertTrue(deleted);

        // Verify deletion
        assertThrows(RuntimeException.class, () -> userService.findById(userId));
    }

    @Test
    void saveMultipleUsers_ShouldPersistAll() {
        User saved1 = userService.save(testUser1);
        User saved2 = userService.save(testUser2);

        assertNotNull(saved1.getUserId());
        assertNotNull(saved2.getUserId());
        assertNotEquals(saved1.getUserId(), saved2.getUserId());

        List<User> allUsers = userService.findAll();
        assertEquals(2, allUsers.size());
    }

    @Test
    void updateAllFields_ShouldReflectChanges() {
        User savedUser = userRepository.save(testUser1);
        String userId = savedUser.getUserId();

        User completeUpdate = new User(null, "NewFirst", "NewLast", "NewCompany", 100, "9999999999");
        User updatedUser = userService.update(userId, completeUpdate);

        assertEquals("NewFirst", updatedUser.getFirstName());
        assertEquals("NewLast", updatedUser.getLastName());
        assertEquals("NewCompany", updatedUser.getCompanyName());
        assertEquals(100, updatedUser.getExperienceMonths());
        assertEquals("9999999999", updatedUser.getMobileNumber());

        // Verify in database
        User dbUser = userRepository.findById(userId).orElse(null);
        assertNotNull(dbUser);
        assertEquals("NewFirst", dbUser.getFirstName());
        assertEquals("NewLast", dbUser.getLastName());
        assertEquals("NewCompany", dbUser.getCompanyName());
        assertEquals(100, dbUser.getExperienceMonths());
        assertEquals("9999999999", dbUser.getMobileNumber());
    }
}
