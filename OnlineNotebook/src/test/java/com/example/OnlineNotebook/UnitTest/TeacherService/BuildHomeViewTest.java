package com.example.OnlineNotebook.UnitTest.TeacherService;

import com.example.OnlineNotebook.models.dtos.teacher.home.TeacherHomeViewDto;
import com.example.OnlineNotebook.models.entities.Assignment;
import com.example.OnlineNotebook.models.entities.Course;
import com.example.OnlineNotebook.models.entities.User;
import com.example.OnlineNotebook.models.enums.AssignmentType;
import com.example.OnlineNotebook.models.enums.UserType;
import com.example.OnlineNotebook.repositories.AssignmentRepository;
import com.example.OnlineNotebook.services.CourseService;
import com.example.OnlineNotebook.services.GradeService;
import com.example.OnlineNotebook.services.TeacherService;
import com.example.OnlineNotebook.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BuildHomeViewTest {

    @InjectMocks
    private TeacherService teacherService;
    @Mock
    private CourseService courseService;
    @Mock
    private UserService userService;
    @Mock
    private AssignmentRepository assignmentRepository;
    @Mock
    private GradeService gradeService;

    @Test
    void buildHomeView_whenTeacherHasNoAssignments_thenShouldReturnEmptyList() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .userType(UserType.TEACHER)
                .build();

        when(assignmentRepository.findByCreatedByOrderByAssignedDateDesc(teacher))
                .thenReturn(Collections.emptyList());

        TeacherHomeViewDto result = teacherService.buildHomeView(teacher);

        assertNotNull(result);
        assertNotNull(result.getAssignments());
        assertTrue(result.getAssignments().isEmpty());
        verify(assignmentRepository, times(1)).findByCreatedByOrderByAssignedDateDesc(teacher);
    }

    @Test
    void buildHomeView_whenTeacherHasAssignments_thenShouldReturnSortedList() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .userType(UserType.TEACHER)
                .build();

        Course course1 = Course.builder()
                .id(UUID.randomUUID())
                .name("Mathematics")
                .teacher(teacher)
                .build();

        Course course2 = Course.builder()
                .id(UUID.randomUUID())
                .name("Physics")
                .teacher(teacher)
                .build();

        LocalDateTime now = LocalDateTime.now();
        Assignment assignment1 = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Math Homework 1")
                .type(AssignmentType.HOMEWORK)
                .dueDate(now.plusDays(7))
                .assignedDate(now.minusDays(2))
                .createdBy(teacher)
                .course(course1)
                .build();

        Assignment assignment2 = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Physics Test Prep")
                .type(AssignmentType.TEST_PREPARATION)
                .dueDate(now.plusDays(5))
                .assignedDate(now.minusDays(1))
                .createdBy(teacher)
                .course(course2)
                .build();

        Assignment assignment3 = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Math Optional Work")
                .type(AssignmentType.OPTIONAL_WORK)
                .dueDate(now.plusDays(10))
                .assignedDate(now.minusDays(3))
                .createdBy(teacher)
                .course(course1)
                .build();

        List<Assignment> assignments = List.of(assignment1, assignment2, assignment3);
        when(assignmentRepository.findByCreatedByOrderByAssignedDateDesc(teacher))
                .thenReturn(assignments);

        TeacherHomeViewDto result = teacherService.buildHomeView(teacher);

        assertNotNull(result);
        assertNotNull(result.getAssignments());
        assertEquals(3, result.getAssignments().size());
        
        assertEquals(assignment1.getId(), result.getAssignments().get(0).getId());
        assertEquals("Math Homework 1", result.getAssignments().get(0).getTitle());
        assertEquals("Mathematics", result.getAssignments().get(0).getCourseName());
        assertEquals("Homework", result.getAssignments().get(0).getAssignmentType());
        assertEquals(assignment1.getDueDate().toLocalDate(), result.getAssignments().get(0).getDueDate());

        assertEquals(assignment2.getId(), result.getAssignments().get(1).getId());
        assertEquals("Physics Test Prep", result.getAssignments().get(1).getTitle());
        assertEquals("Physics", result.getAssignments().get(1).getCourseName());
        assertEquals("Test preparation", result.getAssignments().get(1).getAssignmentType());
        assertEquals(assignment2.getDueDate().toLocalDate(), result.getAssignments().get(1).getDueDate());

        assertEquals(assignment3.getId(), result.getAssignments().get(2).getId());
        assertEquals("Math Optional Work", result.getAssignments().get(2).getTitle());
        assertEquals("Mathematics", result.getAssignments().get(2).getCourseName());
        assertEquals("Optional work", result.getAssignments().get(2).getAssignmentType());
        assertEquals(assignment3.getDueDate().toLocalDate(), result.getAssignments().get(2).getDueDate());

        verify(assignmentRepository, times(1)).findByCreatedByOrderByAssignedDateDesc(teacher);
    }

    @Test
    void buildHomeView_whenAssignmentsAreOrderedByAssignedDateDesc_thenShouldMaintainOrder() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .userType(UserType.TEACHER)
                .build();

        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("Mathematics")
                .teacher(teacher)
                .build();

        LocalDateTime now = LocalDateTime.now();
        Assignment newest = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Newest Assignment")
                .type(AssignmentType.HOMEWORK)
                .dueDate(now.plusDays(7))
                .assignedDate(now.minusDays(1))
                .createdBy(teacher)
                .course(course)
                .build();

        Assignment oldest = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Oldest Assignment")
                .type(AssignmentType.HOMEWORK)
                .dueDate(now.plusDays(7))
                .assignedDate(now.minusDays(5))
                .createdBy(teacher)
                .course(course)
                .build();

        Assignment middle = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Middle Assignment")
                .type(AssignmentType.HOMEWORK)
                .dueDate(now.plusDays(7))
                .assignedDate(now.minusDays(3))
                .createdBy(teacher)
                .course(course)
                .build();

        // Repository returns in descending order (newest first)
        List<Assignment> assignments = List.of(newest, middle, oldest);
        when(assignmentRepository.findByCreatedByOrderByAssignedDateDesc(teacher))
                .thenReturn(assignments);

        TeacherHomeViewDto result = teacherService.buildHomeView(teacher);

        assertNotNull(result);
        assertEquals(3, result.getAssignments().size());
        assertEquals(newest.getId(), result.getAssignments().get(0).getId());
        assertEquals(middle.getId(), result.getAssignments().get(1).getId());
        assertEquals(oldest.getId(), result.getAssignments().get(2).getId());
        verify(assignmentRepository, times(1)).findByCreatedByOrderByAssignedDateDesc(teacher);
    }

    @Test
    void buildHomeView_whenAssignmentHasAllTypes_thenShouldMapAllCorrectly() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .userType(UserType.TEACHER)
                .build();

        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("Mathematics")
                .teacher(teacher)
                .build();

        LocalDateTime now = LocalDateTime.now();
        Assignment homework = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Homework Assignment")
                .type(AssignmentType.HOMEWORK)
                .dueDate(now.plusDays(7))
                .assignedDate(now.minusDays(1))
                .createdBy(teacher)
                .course(course)
                .build();

        Assignment testPrep = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Test Prep Assignment")
                .type(AssignmentType.TEST_PREPARATION)
                .dueDate(now.plusDays(5))
                .assignedDate(now.minusDays(2))
                .createdBy(teacher)
                .course(course)
                .build();

        Assignment optional = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Optional Assignment")
                .type(AssignmentType.OPTIONAL_WORK)
                .dueDate(now.plusDays(10))
                .assignedDate(now.minusDays(3))
                .createdBy(teacher)
                .course(course)
                .build();

        List<Assignment> assignments = List.of(homework, testPrep, optional);
        when(assignmentRepository.findByCreatedByOrderByAssignedDateDesc(teacher))
                .thenReturn(assignments);

        TeacherHomeViewDto result = teacherService.buildHomeView(teacher);

        assertNotNull(result);
        assertEquals(3, result.getAssignments().size());
        
        assertEquals("Homework", result.getAssignments().get(0).getAssignmentType());
        assertEquals("Test preparation", result.getAssignments().get(1).getAssignmentType());
        assertEquals("Optional work", result.getAssignments().get(2).getAssignmentType());
        verify(assignmentRepository, times(1)).findByCreatedByOrderByAssignedDateDesc(teacher);
    }
}

