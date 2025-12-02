package com.example.OnlineNotebook.UnitTest.UserService;

import com.example.OnlineNotebook.exceptions.ResourceNotFoundException;
import com.example.OnlineNotebook.models.entities.User;
import com.example.OnlineNotebook.models.enums.UserType;
import com.example.OnlineNotebook.repositories.AssignmentRepository;
import com.example.OnlineNotebook.repositories.CourseRepository;
import com.example.OnlineNotebook.repositories.UserRepository;
import com.example.OnlineNotebook.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetByIdTest {

    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private AssignmentRepository assignmentRepository;

    @Test
    void getById_whenUserExists_thenReturnUser() {
        UUID userId = UUID.randomUUID();
        
        User user = User.builder()
                .id(userId)
                .firstName("Ivan")
                .lastName("Schweicarov")
                .email("ivan@example.com")
                .userType(UserType.STUDENT)
                .build();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        User result = userService.getById(userId);
        
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("Ivan", result.getFirstName());
        assertEquals("Schweicarov", result.getLastName());
        assertEquals("ivan@example.com", result.getEmail());
        assertEquals(UserType.STUDENT, result.getUserType());
    }

    @Test
    void getById_whenUserNotFound_thenThrowResourceNotFoundException() {
        UUID userId = UUID.randomUUID();
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getById(userId);
        });
        
        assertEquals("User not found with ID: " + userId, exception.getMessage());
    }
}









