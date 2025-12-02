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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetRecentUsersTest {

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
    void getRecentUsers_whenUsersExist_thenReturnFormattedUserList() {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        
        User user1 = User.builder()
                .id(userId1)
                .firstName("Ivan")
                .lastName("Schweicarov")
                .email("ivan@example.com")
                .userType(UserType.STUDENT)
                .createdAt(now.minusDays(1))
                .build();
        
        User user2 = User.builder()
                .id(userId2)
                .firstName("Maria")
                .lastName("Petrova")
                .email("maria@example.com")
                .userType(UserType.TEACHER)
                .createdAt(now)
                .build();
        
        List<User> users = Arrays.asList(user1, user2);
        Page<User> page = new PageImpl<>(users);
        
        when(userRepository.findAll(any(PageRequest.class))).thenReturn(page);
        
        List<Map<String, Object>> result = userService.getRecentUsers(5);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        
        Map<String, Object> firstUser = result.get(0);
        assertEquals(userId1.toString(), firstUser.get("id"));
        assertEquals("Ivan", firstUser.get("firstName"));
        assertEquals("Schweicarov", firstUser.get("lastName"));
        assertEquals("ivan@example.com", firstUser.get("email"));
        assertEquals("STUDENT", firstUser.get("userType"));
        assertNotNull(firstUser.get("createdAt"));
    }

    @Test
    void getRecentUsers_whenNoUsers_thenReturnEmptyList() {
        Page<User> emptyPage = new PageImpl<>(Arrays.asList());
        
        when(userRepository.findAll(any(PageRequest.class))).thenReturn(emptyPage);
        
        List<Map<String, Object>> result = userService.getRecentUsers(5);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}


