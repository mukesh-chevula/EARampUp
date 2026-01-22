package com.example.grpc;

import com.example.grpc.model.User;
import com.example.grpc.proto.*;
import com.example.grpc.repository.UserRepository;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = {
    "spring.cassandra.contact-points=localhost:9042",
    "spring.cassandra.keyspace-name=grpc_keyspace"
})
class GrpcApplicationTests {

    @Autowired
    private UserRepository userRepository;

    private UserServiceGrpc.UserServiceBlockingStub userServiceStub;
    private ManagedChannel channel;

    @BeforeAll
    void setupChannel() {
        channel = ManagedChannelBuilder
                .forAddress("localhost", 9090)
                .usePlaintext()
                .build();
        userServiceStub = UserServiceGrpc.newBlockingStub(channel);
    }

    @AfterAll
    void tearDownChannel() {
        if (channel != null) {
            channel.shutdown();
        }
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void testCreateUser() {
        CreateUserRequest request = CreateUserRequest.newBuilder()
                .setFirstName("Mukesh")
                .setLastName("Ch")
                .setCompany("EA")
                .setAge(24)
                .setPhoneNumber("1234567890")
                .build();

        UserResponse response = userServiceStub.createUser(request);

        assertNotNull(response);
        assertNotNull(response.getUser());
        assertEquals("Mukesh", response.getUser().getFirstName());
        assertEquals("Ch", response.getUser().getLastName());
        assertEquals("EA", response.getUser().getCompany());
        assertEquals(24, response.getUser().getAge());
        assertEquals("1234567890", response.getUser().getPhoneNumber());
    }

    @Test
    void testUpdateUser() {
        // First create a user
        User user = new User("test-id", "Mukesh", "Ch", "EA", 24, "1234567890");
        user = userRepository.save(user);

        // Update the user
        UpdateUserRequest request = UpdateUserRequest.newBuilder()
                .setUserId(user.getUserId())
                .setFirstName("Mukesh")
                .setLastName("Chevula")
                .setCompany("Electronic Arts")
                .setAge(25)
                .setPhoneNumber("9876543210")
                .build();

        UserResponse response = userServiceStub.updateUser(request);

        assertNotNull(response);
        assertEquals("Mukesh", response.getUser().getFirstName());
        assertEquals("Chevula", response.getUser().getLastName());
        assertEquals("Electronic Arts", response.getUser().getCompany());
        assertEquals(25, response.getUser().getAge());
        assertEquals("9876543210", response.getUser().getPhoneNumber());
    }

    @Test
    void testDeleteUser() {
        // First create a user
        User user = new User("test-id", "Daivik", "B", "Qualcomm", 36, "0987654321");
        user = userRepository.save(user);

        // Delete the user
        DeleteUserRequest request = DeleteUserRequest.newBuilder()
                .setUserId(user.getUserId())
                .build();

        DeleteUserResponse response = userServiceStub.deleteUser(request);

        assertTrue(response.getSuccess());
        assertEquals("User deleted successfully", response.getMessage());
        assertFalse(userRepository.existsById(user.getUserId()));
    }

    @Test
    void testGetUserById() {
        // First create a user
        User user = new User("test-id", "Mukesh", "Ch", "EA", 24, "1234567890");
        user = userRepository.save(user);

        // Get the user by ID
        GetUserByIdRequest request = GetUserByIdRequest.newBuilder()
                .setUserId(user.getUserId())
                .build();

        UserResponse response = userServiceStub.getUserById(request);

        assertNotNull(response);
        assertEquals(user.getUserId(), response.getUser().getUserId());
        assertEquals("Mukesh", response.getUser().getFirstName());
        assertEquals("Ch", response.getUser().getLastName());
    }

    @Test
    void testGetAllUsers() {
        // Create multiple users
        User user1 = new User("1", "Mukesh", "Ch", "EA", 24, "1234567890");
        User user2 = new User("2", "Daivik", "B", "Qualcomm", 36, "0987654321");
        userRepository.save(user1);
        userRepository.save(user2);

        // Get all users
        GetAllUsersRequest request = GetAllUsersRequest.newBuilder().build();
        GetAllUsersResponse response = userServiceStub.getAllUsers(request);

        assertNotNull(response);
        assertEquals(2, response.getUsersCount());
        
        assertTrue(response.getUsersList().stream()
                .anyMatch(u -> u.getFirstName().equals("Mukesh")));
        assertTrue(response.getUsersList().stream()
                .anyMatch(u -> u.getFirstName().equals("Daivik")));
    }
}
