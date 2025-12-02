package com.example.OnlineNotebook.UnitTest.UserService;

import com.example.OnlineNotebook.exceptions.ResourceNotFoundException;
import com.example.OnlineNotebook.models.dtos.auth.EditProfileDto;
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
class BuildEditProfileDtoTest {

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
    void buildEditProfileDto_whenUserExists_thenReturnEditProfileDto() {
        UUID userId = UUID.randomUUID();
        String profilePictureUrl = "https://example.com/profile.jpg";
        
        User user = User.builder()
                .id(userId)
                .firstName("Ivan")
                .lastName("Schweicarov")
                .profilePictureUrl(profilePictureUrl)
                .userType(UserType.STUDENT)
                .build();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        EditProfileDto result = userService.buildEditProfileDto(userId);
        
        assertNotNull(result);
        assertEquals("Ivan", result.getFirstName());
        assertEquals("Schweicarov", result.getLastName());
        assertEquals(profilePictureUrl, result.getProfilePictureUrl());
    }

    @Test
    void buildEditProfileDto_whenUserHasNoProfilePicture_thenReturnDtoWithNullUrl() {
        UUID userId = UUID.randomUUID();
        
        User user = User.builder()
                .id(userId)
                .firstName("Ivan")
                .lastName("Schweicarov")
                .profilePictureUrl(null)
                .userType(UserType.STUDENT)
                .build();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        EditProfileDto result = userService.buildEditProfileDto(userId);
        
        assertNotNull(result);
        assertEquals("Ivan", result.getFirstName());
        assertEquals("Schweicarov", result.getLastName());
        assertNull(result.getProfilePictureUrl());
    }

    @Test
    void buildEditProfileDto_whenUserNotFound_thenThrowResourceNotFoundException() {
        UUID userId = UUID.randomUUID();
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.buildEditProfileDto(userId);
        });
        
        assertEquals("User not found with ID: " + userId, exception.getMessage());
    }
}









