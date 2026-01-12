package com.example.project04mongodb_crud.service;

import com.example.project04mongodb_crud.model.User;
import com.example.project04mongodb_crud.utility.UserRepository;
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
        user1 = new User("696489df058b141ca4fcba93", "Daivik", "B", "Amazon", 5, "3456789012");
        user2 = new User("696489eb058b141ca4fcba94", "Mukesh", "CH", "EA", 5, "3456789012");
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
        when(repo.findById("696489df058b141ca4fcba93")).thenReturn(Optional.of(user1));

        User result = service.findById("696489df058b141ca4fcba93");

        assertNotNull(result);
        assertEquals("Daivik", result.getFirstName());
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
        assertEquals("696489df058b141ca4fcba93", result.getUserId());
        verify(repo, times(1)).save(user1);
    }

    @Test
    void update_ShouldReturnUpdatedUser_WhenUserExists() {
        User updatedInfo = new User("696489df058b141ca4fcba93", "Daivik", "B", "Google", 8, "3456789012");
        
        when(repo.findById("696489df058b141ca4fcba93")).thenReturn(Optional.of(user1));
        when(repo.save(any(User.class))).thenReturn(updatedInfo);
        
        User result = service.update("696489df058b141ca4fcba93", updatedInfo);

        assertNotNull(result);
        assertEquals("Google", result.getCompanyName());
        assertEquals(8, result.getExperienceMonths());
        verify(repo, times(1)).save(any(User.class));
    }

    @Test
    void update_ShouldReturnNull_WhenUserDoesNotExist() {
        when(repo.findById("999999")).thenReturn(Optional.empty());

        User result = service.update("999999", user1);

        assertNull(result);
        verify(repo, never()).save(any(User.class));
    }

    @Test
    void delete_ShouldReturnTrue_WhenUserExists() {
        when(repo.existsById("696489df058b141ca4fcba93")).thenReturn(true);
        doNothing().when(repo).deleteById("696489df058b141ca4fcba93");

        boolean result = service.delete("696489df058b141ca4fcba93");

        assertTrue(result);
        verify(repo, times(1)).deleteById("696489df058b141ca4fcba93");
    }

    @Test
    void delete_ShouldReturnFalse_WhenUserDoesNotExist() {
        when(repo.existsById("999999")).thenReturn(false);

        boolean result = service.delete("999999");

        assertFalse(result);
        verify(repo, never()).deleteById(any());
    }
}
