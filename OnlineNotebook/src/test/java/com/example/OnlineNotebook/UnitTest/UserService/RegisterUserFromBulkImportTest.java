package com.example.OnlineNotebook.UnitTest.UserService;

import com.example.OnlineNotebook.models.dtos.admin.BulkImportUserDto;
import com.example.OnlineNotebook.models.entities.Course;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserFromBulkImportTest {

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
    void registerUserFromBulkImport_whenValidDataWithoutStudentClass_thenSaveUser() {
        String email = "test@example.com";
        String password = "password123";
        String encodedPassword = "encodedPassword123";
        
        BulkImportUserDto bulkImportDto = BulkImportUserDto.builder()
                .firstName("Ivan")
                .lastName("Schweicarov")
                .email(email)
                .password(password)
                .userType(UserType.STUDENT)
                .studentClass(null)
                .build();
        
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        userService.registerUserFromBulkImport(bulkImportDto);
        
        verify(userRepository, times(1)).existsByEmail(email);
        verify(passwordEncoder, times(1)).encode(password);
        verify(courseRepository, never()).findByName(any());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUserFromBulkImport_whenValidDataWithStudentClass_thenSaveUserWithCourse() {
        String email = "test@example.com";
        String password = "password123";
        String encodedPassword = "encodedPassword123";
        String studentClass = "10A";
        
        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name(studentClass)
                .build();
        
        BulkImportUserDto bulkImportDto = BulkImportUserDto.builder()
                .firstName("Ivan")
                .lastName("Schweicarov")
                .email(email)
                .password(password)
                .userType(UserType.STUDENT)
                .studentClass(studentClass)
                .build();
        
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(courseRepository.findByName(studentClass)).thenReturn(course);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        userService.registerUserFromBulkImport(bulkImportDto);
        
        verify(userRepository, times(1)).existsByEmail(email);
        verify(passwordEncoder, times(1)).encode(password);
        verify(courseRepository, times(1)).findByName(studentClass);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUserFromBulkImport_whenEmailExists_thenThrowIllegalArgumentException() {
        String email = "existing@example.com";
        
        BulkImportUserDto bulkImportDto = BulkImportUserDto.builder()
                .firstName("Ivan")
                .lastName("Schweicarov")
                .email(email)
                .password("password123")
                .userType(UserType.STUDENT)
                .studentClass("10A")
                .build();
        
        when(userRepository.existsByEmail(email)).thenReturn(true);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUserFromBulkImport(bulkImportDto);
        });
        
        assertEquals("Email already exists: " + email, exception.getMessage());
        verify(userRepository, times(1)).existsByEmail(email);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUserFromBulkImport_whenEmptyStudentClass_thenSaveUserWithoutCourse() {
        String email = "test@example.com";
        String password = "password123";
        String encodedPassword = "encodedPassword123";
        
        BulkImportUserDto bulkImportDto = BulkImportUserDto.builder()
                .firstName("Ivan")
                .lastName("Schweicarov")
                .email(email)
                .password(password)
                .userType(UserType.TEACHER)
                .studentClass("")
                .build();
        
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        userService.registerUserFromBulkImport(bulkImportDto);
        
        verify(userRepository, times(1)).existsByEmail(email);
        verify(passwordEncoder, times(1)).encode(password);
        verify(courseRepository, never()).findByName(any());
        verify(userRepository, times(1)).save(any(User.class));
    }
}









