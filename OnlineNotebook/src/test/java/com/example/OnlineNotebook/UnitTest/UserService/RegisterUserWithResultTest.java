package com.example.OnlineNotebook.UnitTest.UserService;

import com.example.OnlineNotebook.models.dtos.admin.UserRegistrationResult;
import com.example.OnlineNotebook.models.dtos.auth.RegisterDto;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserWithResultTest {

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
    void registerUserWithResult_whenValidData_thenReturnSuccessResult() {
        String email = "test@example.com";
        String password = "password123";
        String encodedPassword = "encodedPassword123";
        
        RegisterDto registerDto = RegisterDto.builder()
                .firstName("Ivan")
                .lastName("Schweicarov")
                .email(email)
                .password(password)
                .userType(UserType.STUDENT)
                .build();
        
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        UserRegistrationResult result = userService.registerUserWithResult(registerDto);
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("User created successfully", result.getMessage());
        verify(userRepository, times(1)).existsByEmail(email);
        verify(passwordEncoder, times(1)).encode(password);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUserWithResult_whenEmailExists_thenThrowIllegalArgumentException() {
        String email = "existing@example.com";
        
        RegisterDto registerDto = RegisterDto.builder()
                .firstName("Ivan")
                .lastName("Schweicarov")
                .email(email)
                .password("password123")
                .userType(UserType.STUDENT)
                .build();
        
        when(userRepository.existsByEmail(email)).thenReturn(true);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUserWithResult(registerDto);
        });
        
        assertEquals("Email already exists: " + email, exception.getMessage());
        verify(userRepository, times(1)).existsByEmail(email);
        verify(userRepository, never()).save(any(User.class));
    }
}









