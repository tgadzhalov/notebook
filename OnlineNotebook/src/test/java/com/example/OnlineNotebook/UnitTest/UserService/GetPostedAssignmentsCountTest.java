package com.example.OnlineNotebook.UnitTest.UserService;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPostedAssignmentsCountTest {

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
    void getPostedAssignmentsCount_whenAssignmentsExist_thenReturnCount() {
        when(assignmentRepository.count()).thenReturn(10L);
        
        long result = userService.getPostedAssignmentsCount();
        
        assertEquals(10, result);
    }

    @Test
    void getPostedAssignmentsCount_whenNoAssignments_thenReturnZero() {
        when(assignmentRepository.count()).thenReturn(0L);
        
        long result = userService.getPostedAssignmentsCount();
        
        assertEquals(0, result);
    }
}









