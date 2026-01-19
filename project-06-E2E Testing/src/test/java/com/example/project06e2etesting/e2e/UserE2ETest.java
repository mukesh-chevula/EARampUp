package com.example.project06e2etesting.e2e;

import com.example.project06e2etesting.model.User;
import com.example.project06e2etesting.utility.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-End tests for the User Management System.
 * These tests simulate real user scenarios by making actual HTTP requests
 * to the running application and verifying the complete flow including database persistence.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    private String baseUrl;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/users";
        userRepository.deleteAll();
        user1 = new User(null, "Mukesh", "Ch", "EA", 24, "1234567890");
        user2 = new User(null, "Daivik", "B", "Qualcomm", 36, "0987654321");
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void completeUserLifecycle_CreateReadUpdateDelete_ShouldWorkEndToEnd() {
        // 1. CREATE - Add a new user
        User newUser = new User(null, user1.getFirstName(), user1.getLastName(), user1.getCompanyName(), user1.getExperienceMonths(), user1.getMobileNumber());
        
        ResponseEntity<User> createResponse = restTemplate.postForEntity(baseUrl, newUser, User.class);
        
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().getUserId()).isNotNull();
        assertThat(createResponse.getBody().getFirstName()).isEqualTo("Mukesh");
        
        String userId = createResponse.getBody().getUserId();

        // 2. READ - Retrieve the created user
        ResponseEntity<User> getResponse = restTemplate.getForEntity(baseUrl + "/" + userId, User.class);
        
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getUserId()).isEqualTo(userId);
        assertThat(getResponse.getBody().getFirstName()).isEqualTo("Mukesh");

        // 3. UPDATE - Modify the user
        User updatedUser = new User(userId, user2.getFirstName(), user2.getLastName(), user2.getCompanyName(), user2.getExperienceMonths(), user2.getMobileNumber());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<User> updateRequest = new HttpEntity<>(updatedUser, headers);
        
        ResponseEntity<User> updateResponse = restTemplate.exchange(
            baseUrl + "/" + userId,
            HttpMethod.PUT,
            updateRequest,
            User.class
        );
        
        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().getLastName()).isEqualTo("B");
        assertThat(updateResponse.getBody().getCompanyName()).isEqualTo("Qualcomm");
        assertThat(updateResponse.getBody().getExperienceMonths()).isEqualTo(36);

        // Verify update persisted
        ResponseEntity<User> verifyResponse = restTemplate.getForEntity(baseUrl + "/" + userId, User.class);
        assertThat(verifyResponse.getBody().getLastName()).isEqualTo("B");

        // 4. DELETE - Remove the user
        ResponseEntity<Boolean> deleteResponse = restTemplate.exchange(
            baseUrl + "/" + userId,
            HttpMethod.DELETE,
            null,
            Boolean.class
        );
        
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(deleteResponse.getBody()).isTrue();

        // 5. VERIFY DELETION - User should no longer exist
        ResponseEntity<User> notFoundResponse = restTemplate.getForEntity(baseUrl + "/" + userId, User.class);
        assertThat(notFoundResponse.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void multipleUsers_CreateAndRetrieveAll_ShouldReturnAllUsers() {
        // Create multiple users
        ResponseEntity<User> response1 = restTemplate.postForEntity(baseUrl, user1, User.class);
        ResponseEntity<User> response2 = restTemplate.postForEntity(baseUrl, user2, User.class);

        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Retrieve all users
        ResponseEntity<List<User>> getAllResponse = restTemplate.exchange(
            baseUrl,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<User>>() {}
        );

        assertThat(getAllResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getAllResponse.getBody()).isNotNull();
        assertThat(getAllResponse.getBody()).hasSize(2);
        assertThat(getAllResponse.getBody())
            .extracting(User::getFirstName)
            .containsExactlyInAnyOrder("Mukesh", "Daivik");
    }

    @Test
    void userWorkflow_CreateUpdateMultipleTimes_ShouldPersistAllChanges() {
        // Initial user creation
        ResponseEntity<User> createResponse = restTemplate.postForEntity(baseUrl, user1, User.class);
        String userId = createResponse.getBody().getUserId();

        // First update - change to user2 data
        User update1 = new User(userId, user2.getFirstName(), user2.getLastName(), user2.getCompanyName(), user2.getExperienceMonths(), user2.getMobileNumber());
        HttpEntity<User> request1 = new HttpEntity<>(update1);
        ResponseEntity<User> updateResponse1 = restTemplate.exchange(
            baseUrl + "/" + userId, HttpMethod.PUT, request1, User.class
        );
        assertThat(updateResponse1.getBody().getCompanyName()).isEqualTo("Qualcomm");
        assertThat(updateResponse1.getBody().getExperienceMonths()).isEqualTo(36);

        // Second update - change phone
        User update2 = new User(userId, user2.getFirstName(), user2.getLastName(), user2.getCompanyName(), user2.getExperienceMonths(), "9999999999");
        HttpEntity<User> request2 = new HttpEntity<>(update2);
        ResponseEntity<User> updateResponse2 = restTemplate.exchange(
            baseUrl + "/" + userId, HttpMethod.PUT, request2, User.class
        );
        assertThat(updateResponse2.getBody().getMobileNumber()).isEqualTo("9999999999");

        // Third update - change back to user1 with updated experience
        User update3 = new User(userId, user1.getFirstName(), user1.getLastName(), user1.getCompanyName(), 30, "9999999999");
        HttpEntity<User> request3 = new HttpEntity<>(update3);
        ResponseEntity<User> updateResponse3 = restTemplate.exchange(
            baseUrl + "/" + userId, HttpMethod.PUT, request3, User.class
        );
        assertThat(updateResponse3.getBody().getLastName()).isEqualTo("Ch");
        assertThat(updateResponse3.getBody().getExperienceMonths()).isEqualTo(30);

        // Final verification - check all changes persisted
        ResponseEntity<User> finalResponse = restTemplate.getForEntity(baseUrl + "/" + userId, User.class);
        User finalUser = finalResponse.getBody();
        assertThat(finalUser.getFirstName()).isEqualTo("Mukesh");
        assertThat(finalUser.getLastName()).isEqualTo("Ch");
        assertThat(finalUser.getCompanyName()).isEqualTo("EA");
        assertThat(finalUser.getExperienceMonths()).isEqualTo(30);
        assertThat(finalUser.getMobileNumber()).isEqualTo("9999999999");
    }

    @Test
    void deletionScenario_DeleteNonExistentUser_ShouldReturnFalse() {
        String nonExistentId = "nonexistent123";
        
        ResponseEntity<Boolean> deleteResponse = restTemplate.exchange(
            baseUrl + "/" + nonExistentId,
            HttpMethod.DELETE,
            null,
            Boolean.class
        );

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(deleteResponse.getBody()).isFalse();
    }

    @Test
    void bulkOperations_CreateMultipleAndDeleteSelectively_ShouldMaintainCorrectState() {
        // Create 2 users
        ResponseEntity<User> response1 = restTemplate.postForEntity(baseUrl, user1, User.class);
        ResponseEntity<User> response2 = restTemplate.postForEntity(baseUrl, user2, User.class);
        String userId1 = response1.getBody().getUserId();
        String userId2 = response2.getBody().getUserId();

        // Verify both users exist
        ResponseEntity<List<User>> getAllResponse = restTemplate.exchange(
            baseUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<User>>() {}
        );
        assertThat(getAllResponse.getBody()).hasSize(2);

        // Delete user1
        restTemplate.exchange(baseUrl + "/" + userId1, HttpMethod.DELETE, null, Boolean.class);

        // Verify 1 user remains
        ResponseEntity<List<User>> remainingResponse = restTemplate.exchange(
            baseUrl, HttpMethod.GET, null, new ParameterizedTypeReference<List<User>>() {}
        );
        assertThat(remainingResponse.getBody()).hasSize(1);
        
        // Verify correct user remains (user2)
        List<User> remainingUsers = remainingResponse.getBody();
        assertThat(remainingUsers)
            .extracting(User::getFirstName)
            .containsExactly("Daivik");
    }

    @Test
    void retrievalScenario_GetSpecificUser_ShouldReturnCorrectUser() {
        // Create 2 users
        String userId1 = restTemplate.postForEntity(baseUrl, user1, User.class).getBody().getUserId();
        String userId2 = restTemplate.postForEntity(baseUrl, user2, User.class).getBody().getUserId();

        // Retrieve specific user
        ResponseEntity<User> response = restTemplate.getForEntity(baseUrl + "/" + userId2, User.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUserId()).isEqualTo(userId2);
        assertThat(response.getBody().getFirstName()).isEqualTo("Daivik");
        assertThat(response.getBody().getCompanyName()).isEqualTo("Qualcomm");
        assertThat(response.getBody().getExperienceMonths()).isEqualTo(36);
    }

    @Test
    void errorHandling_GetNonExistentUser_ShouldReturnError() {
        String nonExistentId = "invalid999";
        
        ResponseEntity<String> response = restTemplate.getForEntity(
            baseUrl + "/" + nonExistentId, 
            String.class
        );

        // Should return error status (500 based on current implementation)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void dataIntegrity_CreateUserAndVerifyDatabasePersistence_ShouldMatchExactly() {
        // Create user via REST API
        ResponseEntity<User> createResponse = restTemplate.postForEntity(baseUrl, user1, User.class);
        String userId = createResponse.getBody().getUserId();

        // Verify via database directly
        User dbUser = userRepository.findById(userId).orElse(null);
        
        assertThat(dbUser).isNotNull();
        assertThat(dbUser.getUserId()).isEqualTo(userId);
        assertThat(dbUser.getFirstName()).isEqualTo("Mukesh");
        assertThat(dbUser.getLastName()).isEqualTo("Ch");
        assertThat(dbUser.getCompanyName()).isEqualTo("EA");
        assertThat(dbUser.getExperienceMonths()).isEqualTo(24);
        assertThat(dbUser.getMobileNumber()).isEqualTo("1234567890");
    }

    @Test
    void concurrentOperations_CreateUpdateRetrieve_ShouldMaintainConsistency() {
        // Scenario: Rapid succession of operations
        // Create
        ResponseEntity<User> created = restTemplate.postForEntity(baseUrl, user2, User.class);
        String userId = created.getBody().getUserId();
        
        // Immediate update - change experience to user1's experience
        User update = new User(userId, user2.getFirstName(), user2.getLastName(), user2.getCompanyName(), user1.getExperienceMonths(), user2.getMobileNumber());
        restTemplate.exchange(baseUrl + "/" + userId, HttpMethod.PUT, new HttpEntity<>(update), User.class);
        
        // Immediate retrieve
        ResponseEntity<User> retrieved = restTemplate.getForEntity(baseUrl + "/" + userId, User.class);
        
        // Verify consistency
        assertThat(retrieved.getBody().getExperienceMonths()).isEqualTo(24);
        
        // Verify in database
        User dbUser = userRepository.findById(userId).orElse(null);
        assertThat(dbUser.getExperienceMonths()).isEqualTo(24);
    }

    @Test
    void emptyDatabase_GetAllUsers_ShouldReturnEmptyList() {
        // Ensure database is empty
        userRepository.deleteAll();
        
        ResponseEntity<List<User>> response = restTemplate.exchange(
            baseUrl,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<User>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEmpty();
    }
}
