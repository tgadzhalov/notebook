package com.example.OnlineNotebook.UnitTest.UserService;

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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTotalStudentsCountTest {

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
    void getTotalStudentsCount_whenStudentsExist_thenReturnCount() {
        User student1 = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.STUDENT)
                .build();
        
        User student2 = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.STUDENT)
                .build();
        
        User student3 = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.STUDENT)
                .build();
        
        List<User> students = Arrays.asList(student1, student2, student3);
        
        when(userRepository.findByUserType(UserType.STUDENT)).thenReturn(students);
        
        long result = userService.getTotalStudentsCount();
        
        assertEquals(3, result);
    }

    @Test
    void getTotalStudentsCount_whenNoStudents_thenReturnZero() {
        List<User> emptyList = Arrays.asList();
        
        when(userRepository.findByUserType(UserType.STUDENT)).thenReturn(emptyList);
        
        long result = userService.getTotalStudentsCount();
        
        assertEquals(0, result);
    }
}









