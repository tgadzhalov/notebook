package com.example.OnlineNotebook.UnitTest.UserService;

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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetStudentsByCourseTest {

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
    void getStudentsByCourse_whenCourseHasStudents_thenReturnOnlyStudents() {
        UUID courseId = UUID.randomUUID();
        Course course = Course.builder()
                .id(courseId)
                .name("Mathematics")
                .build();
        
        User student1 = User.builder()
                .id(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Schweicarov")
                .userType(UserType.STUDENT)
                .course(course)
                .build();
        
        User student2 = User.builder()
                .id(UUID.randomUUID())
                .firstName("Maria")
                .lastName("Petrova")
                .userType(UserType.STUDENT)
                .course(course)
                .build();
        
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .userType(UserType.TEACHER)
                .course(course)
                .build();
        
        List<User> allUsers = Arrays.asList(student1, student2, teacher);
        
        when(userRepository.findByCourse(course)).thenReturn(allUsers);
        
        List<User> result = userService.getStudentsByCourse(course);
        
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(user -> user.getUserType() == UserType.STUDENT));
        assertTrue(result.contains(student1));
        assertTrue(result.contains(student2));
        assertFalse(result.contains(teacher));
    }

    @Test
    void getStudentsByCourse_whenCourseHasNoStudents_thenReturnEmptyList() {
        UUID courseId = UUID.randomUUID();
        Course course = Course.builder()
                .id(courseId)
                .name("Mathematics")
                .build();
        
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .userType(UserType.TEACHER)
                .course(course)
                .build();
        
        List<User> allUsers = Arrays.asList(teacher);
        
        when(userRepository.findByCourse(course)).thenReturn(allUsers);
        
        List<User> result = userService.getStudentsByCourse(course);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getStudentsByCourse_whenCourseIsNull_thenReturnEmptyList() {
        when(userRepository.findByCourse(null)).thenReturn(Arrays.asList());
        
        List<User> result = userService.getStudentsByCourse(null);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}









