package com.example.OnlineNotebook.UnitTest.UserService;

import com.example.OnlineNotebook.models.dtos.admin.BulkImportUserDto;
import com.example.OnlineNotebook.models.dtos.admin.BulkRegistrationResult;
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

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterBulkUsersTest {

    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void registerBulkUsers_whenAllUsersValid_thenReturnSuccessResult() {
        BulkImportUserDto user1 = BulkImportUserDto.builder()
                .firstName("Ivan")
                .lastName("Schweicarov")
                .email("ivan@example.com")
                .password("password123")
                .userType(UserType.STUDENT)
                .build();
        
        BulkImportUserDto user2 = BulkImportUserDto.builder()
                .firstName("Maria")
                .lastName("Petrova")
                .email("maria@example.com")
                .password("password456")
                .userType(UserType.TEACHER)
                .build();
        
        List<BulkImportUserDto> users = Arrays.asList(user1, user2);
        
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        BulkRegistrationResult result = userService.registerBulkUsers(users);
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Processed 2 users: 2 successful, 0 failed", result.getMessage());
        verify(userRepository, times(2)).save(any(User.class));
    }

    @Test
    void registerBulkUsers_whenSomeUsersHaveDuplicateEmails_thenReturnPartialSuccessResult() {
        BulkImportUserDto user1 = BulkImportUserDto.builder()
                .firstName("Ivan")
                .lastName("Schweicarov")
                .email("ivan@example.com")
                .password("password123")
                .userType(UserType.STUDENT)
                .build();
        
        BulkImportUserDto user2 = BulkImportUserDto.builder()
                .firstName("Maria")
                .lastName("Petrova")
                .email("existing@example.com")
                .password("password456")
                .userType(UserType.TEACHER)
                .build();
        
        List<BulkImportUserDto> users = Arrays.asList(user1, user2);
        
        when(userRepository.existsByEmail("ivan@example.com")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        BulkRegistrationResult result = userService.registerBulkUsers(users);
        
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Processed 2 users: 1 successful, 1 failed", result.getMessage());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerBulkUsers_whenAllUsersHaveDuplicateEmails_thenReturnFailureResult() {
        BulkImportUserDto user1 = BulkImportUserDto.builder()
                .firstName("Ivan")
                .lastName("Schweicarov")
                .email("existing1@example.com")
                .password("password123")
                .userType(UserType.STUDENT)
                .build();
        
        BulkImportUserDto user2 = BulkImportUserDto.builder()
                .firstName("Maria")
                .lastName("Petrova")
                .email("existing2@example.com")
                .password("password456")
                .userType(UserType.TEACHER)
                .build();
        
        List<BulkImportUserDto> users = Arrays.asList(user1, user2);
        
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        
        BulkRegistrationResult result = userService.registerBulkUsers(users);
        
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Processed 2 users: 0 successful, 2 failed", result.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerBulkUsers_whenEmptyList_thenReturnSuccessResult() {
        List<BulkImportUserDto> users = Arrays.asList();
        
        BulkRegistrationResult result = userService.registerBulkUsers(users);
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Processed 0 users: 0 successful, 0 failed", result.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}









