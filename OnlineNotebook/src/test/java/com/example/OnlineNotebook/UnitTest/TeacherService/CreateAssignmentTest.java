package com.example.OnlineNotebook.UnitTest.TeacherService;

import com.example.OnlineNotebook.models.dtos.teacher.assignment.AssignmentFormDto;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CreateAssignmentTest {

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
    void createAssignment_whenValidData_thenShouldCreateAssignment() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Blagoev")
                .email("ivan.blagoev@example.com")
                .userType(UserType.TEACHER)
                .build();

        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("Test Kurs")
                .teacher(teacher)
                .build();

        AssignmentFormDto formDto = AssignmentFormDto.builder()
                .courseId(course.getId())
                .title("Zadanie 1")
                .description("Opisanie na zadanie")
                .assignmentType(AssignmentType.HOMEWORK)
                .dueDate(LocalDate.now().plusDays(7))
                .build();

        when(courseService.getCourseById(course.getId())).thenReturn(course);
        when(assignmentRepository.save(any(Assignment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Assignment result = teacherService.createAssignment(formDto, teacher);

        assertNotNull(result);
        assertEquals("Zadanie 1", result.getTitle());
        assertEquals("Opisanie na zadanie", result.getDescription());
        assertEquals(AssignmentType.HOMEWORK, result.getType());
        assertEquals(teacher.getId(), result.getCreatedBy().getId());
        assertEquals(course.getId(), result.getCourse().getId());
        verify(courseService, times(1)).getCourseById(course.getId());
        verify(assignmentRepository, times(1)).save(any(Assignment.class));
    }

    @Test
    void createAssignment_whenCourseIdIsNull_thenShouldThrowException() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Blagoev")
                .email("ivan.blagoev@example.com")
                .userType(UserType.TEACHER)
                .build();

        AssignmentFormDto formDto = AssignmentFormDto.builder()
                .courseId(null)
                .title("Zadanie 1")
                .assignmentType(AssignmentType.HOMEWORK)
                .dueDate(LocalDate.now().plusDays(7))
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            teacherService.createAssignment(formDto, teacher);
        });

        verify(courseService, never()).getCourseById(any());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void createAssignment_whenCourseNotFound_thenShouldThrowException() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Blagoev")
                .email("ivan.blagoev@example.com")
                .userType(UserType.TEACHER)
                .build();

        UUID courseId = UUID.randomUUID();
        AssignmentFormDto formDto = AssignmentFormDto.builder()
                .courseId(courseId)
                .title("Zadanie 1")
                .assignmentType(AssignmentType.HOMEWORK)
                .dueDate(LocalDate.now().plusDays(7))
                .build();

        when(courseService.getCourseById(courseId)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> {
            teacherService.createAssignment(formDto, teacher);
        });

        verify(courseService, times(1)).getCourseById(courseId);
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void createAssignment_whenTeacherNotOwner_thenShouldThrowException() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Blagoev")
                .email("ivan.blagoev@example.com")
                .userType(UserType.TEACHER)
                .build();

        User otherTeacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("Krum")
                .lastName("Krumov")
                .email("krum.krumov@example.com")
                .userType(UserType.TEACHER)
                .build();

        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("Test Kurs")
                .teacher(otherTeacher)
                .build();

        AssignmentFormDto formDto = AssignmentFormDto.builder()
                .courseId(course.getId())
                .title("Zadanie 1")
                .assignmentType(AssignmentType.HOMEWORK)
                .dueDate(LocalDate.now().plusDays(7))
                .build();

        when(courseService.getCourseById(course.getId())).thenReturn(course);

        assertThrows(IllegalArgumentException.class, () -> {
            teacherService.createAssignment(formDto, teacher);
        });

        verify(courseService, times(1)).getCourseById(course.getId());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void createAssignment_whenAssignmentTypeIsNull_thenShouldThrowException() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Blagoev")
                .email("ivan.blagoev@example.com")
                .userType(UserType.TEACHER)
                .build();

        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("Test Kurs")
                .teacher(teacher)
                .build();

        AssignmentFormDto formDto = AssignmentFormDto.builder()
                .courseId(course.getId())
                .title("Zadanie 1")
                .assignmentType(null)
                .dueDate(LocalDate.now().plusDays(7))
                .build();

        when(courseService.getCourseById(course.getId())).thenReturn(course);

        assertThrows(IllegalArgumentException.class, () -> {
            teacherService.createAssignment(formDto, teacher);
        });

        verify(courseService, times(1)).getCourseById(course.getId());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void createAssignment_whenDueDateIsNull_thenShouldThrowException() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Blagoev")
                .email("ivan.blagoev@example.com")
                .userType(UserType.TEACHER)
                .build();

        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("Test Kurs")
                .teacher(teacher)
                .build();

        AssignmentFormDto formDto = AssignmentFormDto.builder()
                .courseId(course.getId())
                .title("Zadanie 1")
                .assignmentType(AssignmentType.HOMEWORK)
                .dueDate(null)
                .build();

        when(courseService.getCourseById(course.getId())).thenReturn(course);

        assertThrows(IllegalArgumentException.class, () -> {
            teacherService.createAssignment(formDto, teacher);
        });

        verify(courseService, times(1)).getCourseById(course.getId());
        verify(assignmentRepository, never()).save(any());
    }

    @Test
    void createAssignment_whenCreated_thenShouldSetAssignedDate() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Blagoev")
                .email("ivan.blagoev@example.com")
                .userType(UserType.TEACHER)
                .build();

        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("Test Kurs")
                .teacher(teacher)
                .build();

        AssignmentFormDto formDto = AssignmentFormDto.builder()
                .courseId(course.getId())
                .title("Zadanie 1")
                .description("Opisanie")
                .assignmentType(AssignmentType.HOMEWORK)
                .dueDate(LocalDate.now().plusDays(7))
                .build();

        when(courseService.getCourseById(course.getId())).thenReturn(course);
        ArgumentCaptor<Assignment> assignmentCaptor = ArgumentCaptor.forClass(Assignment.class);
        when(assignmentRepository.save(assignmentCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        teacherService.createAssignment(formDto, teacher);

        Assignment savedAssignment = assignmentCaptor.getValue();
        assertNotNull(savedAssignment.getAssignedDate());
        assertTrue(savedAssignment.getAssignedDate().isBefore(LocalDateTime.now().plusSeconds(1)));
        verify(assignmentRepository, times(1)).save(any(Assignment.class));
    }
}

