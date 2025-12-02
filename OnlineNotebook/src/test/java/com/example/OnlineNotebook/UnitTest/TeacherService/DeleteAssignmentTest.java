package com.example.OnlineNotebook.UnitTest.TeacherService;

import com.example.OnlineNotebook.exceptions.ResourceNotFoundException;
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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeleteAssignmentTest {

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
    void deleteAssignment_whenValidData_thenShouldDeleteAssignment() {
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

        Assignment assignment = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Zadanie 1")
                .type(AssignmentType.HOMEWORK)
                .course(course)
                .createdBy(teacher)
                .dueDate(LocalDateTime.now().plusDays(7))
                .assignedDate(LocalDateTime.now())
                .build();

        when(assignmentRepository.findById(assignment.getId())).thenReturn(Optional.of(assignment));
        doNothing().when(assignmentRepository).delete(assignment);

        assertDoesNotThrow(() -> {
            teacherService.deleteAssignment(assignment.getId(), teacher);
        });

        verify(assignmentRepository, times(1)).findById(assignment.getId());
        verify(assignmentRepository, times(1)).delete(assignment);
    }

    @Test
    void deleteAssignment_whenAssignmentNotFound_thenShouldThrowException() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Blagoev")
                .email("ivan.blagoev@example.com")
                .userType(UserType.TEACHER)
                .build();

        UUID assignmentId = UUID.randomUUID();

        when(assignmentRepository.findById(assignmentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            teacherService.deleteAssignment(assignmentId, teacher);
        });

        verify(assignmentRepository, times(1)).findById(assignmentId);
        verify(assignmentRepository, never()).delete(any());
    }

    @Test
    void deleteAssignment_whenTeacherNotOwner_thenShouldThrowException() {
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
                .teacher(teacher)
                .build();

        Assignment assignment = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Zadanie 1")
                .type(AssignmentType.HOMEWORK)
                .course(course)
                .createdBy(teacher)
                .dueDate(LocalDateTime.now().plusDays(7))
                .assignedDate(LocalDateTime.now())
                .build();

        when(assignmentRepository.findById(assignment.getId())).thenReturn(Optional.of(assignment));

        assertThrows(IllegalArgumentException.class, () -> {
            teacherService.deleteAssignment(assignment.getId(), otherTeacher);
        });

        verify(assignmentRepository, times(1)).findById(assignment.getId());
        verify(assignmentRepository, never()).delete(any());
    }
}



