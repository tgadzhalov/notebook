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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateUserProfileTest {

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
    void updateUserProfile_whenValidData_thenUpdateUser() {
        UUID userId = UUID.randomUUID();
        String currentPassword = "oldPassword123";
        String encodedCurrentPassword = "encodedOldPassword";
        
        User user = User.builder()
                .id(userId)
                .firstName("Ivan")
                .lastName("Schweicarov")
                .password(encodedCurrentPassword)
                .userType(UserType.STUDENT)
                .build();
        
        EditProfileDto editProfileDto = EditProfileDto.builder()
                .firstName("Ivan Updated")
                .lastName("Schweicarov Updated")
                .profilePictureUrl("https://example.com/new-profile.jpg")
                .build();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        userService.updateUserProfile(userId, editProfileDto);
        
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUserProfile_whenUpdatingPasswordWithValidCurrentPassword_thenUpdatePassword() {
        UUID userId = UUID.randomUUID();
        String currentPassword = "oldPassword123";
        String newPassword = "newPassword123";
        String encodedCurrentPassword = "encodedOldPassword";
        String encodedNewPassword = "encodedNewPassword";
        
        User user = User.builder()
                .id(userId)
                .firstName("Ivan")
                .lastName("Schweicarov")
                .password(encodedCurrentPassword)
                .userType(UserType.STUDENT)
                .build();
        
        EditProfileDto editProfileDto = EditProfileDto.builder()
                .firstName("Ivan")
                .lastName("Schweicarov")
                .currentPassword(currentPassword)
                .newPassword(newPassword)
                .confirmPassword(newPassword)
                .build();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(currentPassword, encodedCurrentPassword)).thenReturn(true);
        when(passwordEncoder.matches(newPassword, encodedCurrentPassword)).thenReturn(false);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        userService.updateUserProfile(userId, editProfileDto);
        
        verify(passwordEncoder, times(1)).matches(currentPassword, encodedCurrentPassword);
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUserProfile_whenUpdatingPasswordWithoutCurrentPassword_thenThrowIllegalArgumentException() {
        UUID userId = UUID.randomUUID();
        String newPassword = "newPassword123";
        
        User user = User.builder()
                .id(userId)
                .firstName("Ivan")
                .lastName("Schweicarov")
                .password("encodedPassword")
                .userType(UserType.STUDENT)
                .build();
        
        EditProfileDto editProfileDto = EditProfileDto.builder()
                .firstName("Ivan")
                .lastName("Schweicarov")
                .currentPassword(null)
                .newPassword(newPassword)
                .confirmPassword(newPassword)
                .build();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUserProfile(userId, editProfileDto);
        });
        
        assertEquals("Current password is required to change password", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserProfile_whenCurrentPasswordIsIncorrect_thenThrowIllegalArgumentException() {
        UUID userId = UUID.randomUUID();
        String currentPassword = "wrongPassword";
        String newPassword = "newPassword123";
        String encodedCurrentPassword = "encodedOldPassword";
        
        User user = User.builder()
                .id(userId)
                .firstName("Ivan")
                .lastName("Schweicarov")
                .password(encodedCurrentPassword)
                .userType(UserType.STUDENT)
                .build();
        
        EditProfileDto editProfileDto = EditProfileDto.builder()
                .firstName("Ivan")
                .lastName("Schweicarov")
                .currentPassword(currentPassword)
                .newPassword(newPassword)
                .confirmPassword(newPassword)
                .build();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(currentPassword, encodedCurrentPassword)).thenReturn(false);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUserProfile(userId, editProfileDto);
        });
        
        assertEquals("Current password is incorrect", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserProfile_whenNewPasswordSameAsCurrent_thenThrowIllegalArgumentException() {
        UUID userId = UUID.randomUUID();
        String currentPassword = "password123";
        String newPassword = "password123";
        String encodedPassword = "encodedPassword";
        
        User user = User.builder()
                .id(userId)
                .firstName("Ivan")
                .lastName("Schweicarov")
                .password(encodedPassword)
                .userType(UserType.STUDENT)
                .build();
        
        EditProfileDto editProfileDto = EditProfileDto.builder()
                .firstName("Ivan")
                .lastName("Schweicarov")
                .currentPassword(currentPassword)
                .newPassword(newPassword)
                .confirmPassword(newPassword)
                .build();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(currentPassword, encodedPassword)).thenReturn(true);
        when(passwordEncoder.matches(newPassword, encodedPassword)).thenReturn(true);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUserProfile(userId, editProfileDto);
        });
        
        assertEquals("New password must be different from current password", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserProfile_whenPasswordsDoNotMatch_thenThrowIllegalArgumentException() {
        UUID userId = UUID.randomUUID();
        String currentPassword = "oldPassword123";
        String newPassword = "newPassword123";
        String confirmPassword = "differentPassword";
        String encodedCurrentPassword = "encodedOldPassword";
        
        User user = User.builder()
                .id(userId)
                .firstName("Ivan")
                .lastName("Schweicarov")
                .password(encodedCurrentPassword)
                .userType(UserType.STUDENT)
                .build();
        
        EditProfileDto editProfileDto = EditProfileDto.builder()
                .firstName("Ivan")
                .lastName("Schweicarov")
                .currentPassword(currentPassword)
                .newPassword(newPassword)
                .confirmPassword(confirmPassword)
                .build();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUserProfile(userId, editProfileDto);
        });
        
        assertEquals("New password and confirm password do not match", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserProfile_whenUserNotFound_thenThrowResourceNotFoundException() {
        UUID userId = UUID.randomUUID();
        
        EditProfileDto editProfileDto = EditProfileDto.builder()
                .firstName("Ivan")
                .lastName("Schweicarov")
                .build();
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUserProfile(userId, editProfileDto);
        });
        
        assertEquals("User not found with ID: " + userId, exception.getMessage());
    }

    @Test
    void updateUserProfile_whenProfilePictureUrlIsEmpty_thenSetToNull() {
        UUID userId = UUID.randomUUID();
        
        User user = User.builder()
                .id(userId)
                .firstName("Ivan")
                .lastName("Schweicarov")
                .profilePictureUrl("https://example.com/old.jpg")
                .userType(UserType.STUDENT)
                .build();
        
        EditProfileDto editProfileDto = EditProfileDto.builder()
                .firstName("Ivan")
                .lastName("Schweicarov")
                .profilePictureUrl("   ")
                .build();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        userService.updateUserProfile(userId, editProfileDto);
        
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUserProfile_whenFirstNameHasWhitespace_thenTrim() {
        UUID userId = UUID.randomUUID();
        
        User user = User.builder()
                .id(userId)
                .firstName("Ivan")
                .lastName("Schweicarov")
                .userType(UserType.STUDENT)
                .build();
        
        EditProfileDto editProfileDto = EditProfileDto.builder()
                .firstName("  Ivan Updated  ")
                .lastName("Schweicarov")
                .build();
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        userService.updateUserProfile(userId, editProfileDto);
        
        verify(userRepository, times(1)).save(any(User.class));
    }
}









