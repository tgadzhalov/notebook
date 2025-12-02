package com.example.OnlineNotebook.UnitTest.UserService;

import com.example.OnlineNotebook.exceptions.ResourceNotFoundException;
import com.example.OnlineNotebook.models.entities.User;
import com.example.OnlineNotebook.models.enums.UserType;
import com.example.OnlineNotebook.repositories.AssignmentRepository;
import com.example.OnlineNotebook.repositories.CourseRepository;
import com.example.OnlineNotebook.repositories.UserRepository;
import com.example.OnlineNotebook.security.UserData;
import com.example.OnlineNotebook.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoadUserByUsernameTest {

    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;


    @Test
    void loadUserByUsername_whenUserExists_thenReturnUserDetails() {
        String email = "test@example.com";
        UUID userId = UUID.randomUUID();
        String password = "encodedPassword";
        
        User user = User.builder()
                .id(userId)
                .email(email)
                .password(password)
                .userType(UserType.STUDENT)
                .build();
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        
        UserDetails result = userService.loadUserByUsername(email);
        
        assertNotNull(result);
        assertTrue(result instanceof UserData);
        UserData userData = (UserData) result;
        assertEquals(userId, userData.getId());
        assertEquals(email, userData.getUsername());
        assertEquals(password, userData.getPassword());
        assertEquals(UserType.STUDENT, userData.getUserType());
    }

    @Test
    void loadUserByUsername_whenUserNotFound_thenThrowResourceNotFoundException() {
        String email = "nonexistent@example.com";
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.loadUserByUsername(email);
        });
        
        assertEquals("Email not found: " + email, exception.getMessage());
    }
}









