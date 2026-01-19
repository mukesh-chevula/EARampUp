package com.example.project06e2etesting.service;

import com.example.project06e2etesting.model.User;
import com.example.project06e2etesting.utility.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repo;

    @InjectMocks
    private UserService service;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User("1", "Mukesh", "Ch", "EA", 24, "1234567890");
        user2 = new User("2", "Daivik", "B", "Qualcomm", 36, "0987654321");
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        when(repo.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<User> result = service.findAll();

        assertEquals(2, result.size());
        verify(repo, times(1)).findAll();
    }

    @Test
    void findById_ShouldReturnUser_WhenUserExists() {
        when(repo.findById("1")).thenReturn(Optional.of(user1));

        User result = service.findById("1");

        assertNotNull(result);
        assertEquals("Mukesh", result.getFirstName());
    }

    @Test
    void findById_ShouldThrowRuntimeException_WhenUserDoesNotExist() {
        when(repo.findById("99")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.findById("99"));
    }

    @Test
    void save_ShouldReturnSavedUser() {
        when(repo.save(any(User.class))).thenReturn(user1);

        User result = service.save(user1);

        assertNotNull(result);
        assertEquals("1", result.getUserId());
        verify(repo, times(1)).save(user1);
    }

    @Test
    void update_ShouldReturnUpdatedUser_WhenUserExists() {
        User updatedInfo = new User("1", user2.getFirstName(), user2.getLastName(), user2.getCompanyName(), user2.getExperienceMonths(), user2.getMobileNumber());
        
        when(repo.findById("1")).thenReturn(Optional.of(user1));
        when(repo.save(any(User.class))).thenReturn(updatedInfo);
        
        User result = service.update("1", updatedInfo);

        assertNotNull(result);
        assertEquals("Qualcomm", result.getCompanyName());
        assertEquals(36, result.getExperienceMonths());
        verify(repo, times(1)).save(any(User.class));
    }

    @Test
    void update_ShouldReturnNull_WhenUserDoesNotExist() {
        when(repo.findById("99")).thenReturn(Optional.empty());

        User result = service.update("99", user1);

        assertNull(result);
        verify(repo, never()).save(any(User.class));
    }

    @Test
    void delete_ShouldReturnTrue_WhenUserExists() {
        when(repo.existsById("1")).thenReturn(true);

        Boolean result = service.delete("1");

        assertTrue(result);
        verify(repo, times(1)).deleteById("1");
    }

    @Test
    void delete_ShouldReturnFalse_WhenUserDoesNotExist() {
        when(repo.existsById("99")).thenReturn(false);

        Boolean result = service.delete("99");

        assertFalse(result);
        verify(repo, never()).deleteById("99");
    }
}
