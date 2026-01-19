package com.example.project06e2etesting.integration;

import com.example.project06e2etesting.model.User;
import com.example.project06e2etesting.utility.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserRepository.
 * Tests MongoDB repository operations with actual database.
 */
class UserRepositoryIntegrationTest extends BaseIntegrationTest {

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
    void save_ShouldPersistUserToMongoDB() {
        User savedUser = userRepository.save(testUser1);

        assertNotNull(savedUser.getUserId());
        assertEquals("Mukesh", savedUser.getFirstName());
        assertEquals("Ch", savedUser.getLastName());
        assertEquals("EA", savedUser.getCompanyName());
        assertEquals(24, savedUser.getExperienceMonths());
        assertEquals("1234567890", savedUser.getMobileNumber());
    }

    @Test
    void findById_ShouldRetrieveUserFromMongoDB() {
        User savedUser = userRepository.save(testUser1);

        Optional<User> foundUser = userRepository.findById(savedUser.getUserId());

        assertTrue(foundUser.isPresent());
        assertEquals(savedUser.getUserId(), foundUser.get().getUserId());
        assertEquals("Mukesh", foundUser.get().getFirstName());
    }

    @Test
    void findById_WhenUserDoesNotExist_ShouldReturnEmpty() {
        Optional<User> foundUser = userRepository.findById("507f1f77bcf86cd799439011");
        assertFalse(foundUser.isPresent());
    }

    @Test
    void findAll_ShouldRetrieveAllUsersFromMongoDB() {
        userRepository.save(testUser1);
        userRepository.save(testUser2);

        List<User> users = userRepository.findAll();

        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getFirstName().equals("Mukesh")));
        assertTrue(users.stream().anyMatch(u -> u.getFirstName().equals("Daivik")));
    }

    @Test
    void findAll_WhenNoUsers_ShouldReturnEmptyList() {
        List<User> users = userRepository.findAll();
        assertTrue(users.isEmpty());
    }

    @Test
    void existsById_ShouldReturnTrueWhenUserExists() {
        User savedUser = userRepository.save(testUser1);

        boolean exists = userRepository.existsById(savedUser.getUserId());

        assertTrue(exists);
    }

    @Test
    void existsById_ShouldReturnFalseWhenUserDoesNotExist() {
        boolean exists = userRepository.existsById("507f1f77bcf86cd799439011");
        assertFalse(exists);
    }

    @Test
    void deleteById_ShouldRemoveUserFromMongoDB() {
        User savedUser = userRepository.save(testUser1);
        assertTrue(userRepository.existsById(savedUser.getUserId()));

        userRepository.deleteById(savedUser.getUserId());

        assertFalse(userRepository.existsById(savedUser.getUserId()));
    }

    @Test
    void delete_ShouldRemoveUserFromMongoDB() {
        User savedUser = userRepository.save(testUser1);
        assertTrue(userRepository.existsById(savedUser.getUserId()));

        userRepository.delete(savedUser);

        assertFalse(userRepository.existsById(savedUser.getUserId()));
    }

    @Test
    void deleteAll_ShouldRemoveAllUsersFromMongoDB() {
        userRepository.save(testUser1);
        userRepository.save(testUser2);
        assertEquals(2, userRepository.count());

        userRepository.deleteAll();

        assertEquals(0, userRepository.count());
    }

    @Test
    void count_ShouldReturnCorrectNumberOfUsers() {
        assertEquals(0, userRepository.count());

        userRepository.save(testUser1);
        assertEquals(1, userRepository.count());

        userRepository.save(testUser2);
        assertEquals(2, userRepository.count());
    }

    @Test
    void update_ShouldModifyExistingUserInMongoDB() {
        User savedUser = userRepository.save(testUser1);
        String userId = savedUser.getUserId();

        savedUser.setLastName("Updated");
        savedUser.setCompanyName("Electronic Arts");
        savedUser.setExperienceMonths(50);
        
        User updatedUser = userRepository.save(savedUser);

        assertEquals(userId, updatedUser.getUserId());
        assertEquals("Updated", updatedUser.getLastName());
        assertEquals("Electronic Arts", updatedUser.getCompanyName());
        assertEquals(50, updatedUser.getExperienceMonths());

        // Verify changes persisted
        Optional<User> retrievedUser = userRepository.findById(userId);
        assertTrue(retrievedUser.isPresent());
        assertEquals("Updated", retrievedUser.get().getLastName());
        assertEquals("Electronic Arts", retrievedUser.get().getCompanyName());
    }

    @Test
    void saveAll_ShouldPersistMultipleUsers() {
        List<User> users = List.of(testUser1, testUser2);

        List<User> savedUsers = userRepository.saveAll(users);

        assertEquals(2, savedUsers.size());
        assertEquals(2, userRepository.count());
    }

    @Test
    void findAllById_ShouldRetrieveSpecificUsers() {
        User saved1 = userRepository.save(testUser1);
        User saved2 = userRepository.save(testUser2);

        List<User> foundUsers = userRepository.findAllById(List.of(saved1.getUserId(), saved2.getUserId()));

        assertEquals(2, foundUsers.size());
        assertTrue(foundUsers.stream().anyMatch(u -> u.getUserId().equals(saved1.getUserId())));
        assertTrue(foundUsers.stream().anyMatch(u -> u.getUserId().equals(saved2.getUserId())));
    }

    @Test
    void mongoDBAutoGeneratesUserId() {
        User user = new User(null, "Test", "User", "TestCo", 10, "0000000000");
        assertNull(user.getUserId());

        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getUserId());
        assertTrue(savedUser.getUserId().length() > 0);
    }

    @Test
    void multipleOperations_ShouldMaintainDataIntegrity() {
        // Save
        User saved = userRepository.save(testUser1);
        assertNotNull(saved.getUserId());

        // Find
        Optional<User> found = userRepository.findById(saved.getUserId());
        assertTrue(found.isPresent());

        // Update
        found.get().setCompanyName("Updated Company");
        User updated = userRepository.save(found.get());
        assertEquals("Updated Company", updated.getCompanyName());

        // Verify
        Optional<User> verified = userRepository.findById(saved.getUserId());
        assertTrue(verified.isPresent());
        assertEquals("Updated Company", verified.get().getCompanyName());

        // Delete
        userRepository.deleteById(saved.getUserId());
        assertFalse(userRepository.existsById(saved.getUserId()));
    }
}
