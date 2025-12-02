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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailExistsTest {

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
    void emailExists_whenEmailExists_thenReturnTrue() {
        String email = "test@example.com";
        
        when(userRepository.existsByEmail(email)).thenReturn(true);
        
        boolean result = userService.emailExists(email);
        
        assertTrue(result);
    }

    @Test
    void emailExists_whenEmailDoesNotExist_thenReturnFalse() {
        String email = "new@example.com";
        
        when(userRepository.existsByEmail(email)).thenReturn(false);
        
        boolean result = userService.emailExists(email);
        
        assertFalse(result);
    }
}









