package com.example.OnlineNotebook.UnitTest.TeacherService;

import com.example.OnlineNotebook.models.dtos.teacher.assignment.TeacherAssignmentsViewDto;
import com.example.OnlineNotebook.models.entities.Course;
import com.example.OnlineNotebook.models.entities.User;
import com.example.OnlineNotebook.models.enums.AssignmentType;
import com.example.OnlineNotebook.models.enums.UserType;
import com.example.OnlineNotebook.services.CourseService;
import com.example.OnlineNotebook.services.GradeService;
import com.example.OnlineNotebook.services.TeacherService;
import com.example.OnlineNotebook.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BuildAssignmentsViewTest {

    @InjectMocks
    private TeacherService teacherService;
    @Mock
    private CourseService courseService;
    @Mock
    private UserService userService;
    @Mock
    private com.example.OnlineNotebook.repositories.AssignmentRepository assignmentRepository;
    @Mock
    private GradeService gradeService;

    @Test
    void buildAssignmentsView_whenNoCourses_thenShouldReturnEmptyForm() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Blagoev")
                .email("ivan.blagoev@example.com")
                .userType(UserType.TEACHER)
                .build();

        when(courseService.getCoursesByTeacher(teacher)).thenReturn(Collections.emptyList());

        TeacherAssignmentsViewDto result = teacherService.buildAssignmentsView(teacher);

        assertNotNull(result);
        assertNotNull(result.getForm());
        assertNull(result.getForm().getCourseId());
        assertTrue(result.getCourses().isEmpty());
        assertNotNull(result.getAssignmentTypes());
        assertEquals(AssignmentType.values().length, result.getAssignmentTypes().size());
        verify(courseService, times(1)).getCoursesByTeacher(teacher);
    }

    @Test
    void buildAssignmentsView_whenCoursesExist_thenShouldSetFirstCourse() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Blagoev")
                .email("ivan.blagoev@example.com")
                .userType(UserType.TEACHER)
                .build();

        Course course1 = Course.builder()
                .id(UUID.randomUUID())
                .name("Test Kurs")
                .teacher(teacher)
                .build();

        Course course2 = Course.builder()
                .id(UUID.randomUUID())
                .name("Drug Kurs")
                .teacher(teacher)
                .build();

        when(courseService.getCoursesByTeacher(teacher)).thenReturn(List.of(course1, course2));

        TeacherAssignmentsViewDto result = teacherService.buildAssignmentsView(teacher);

        assertNotNull(result);
        assertNotNull(result.getForm());
        assertEquals(course1.getId(), result.getForm().getCourseId());
        assertEquals(2, result.getCourses().size());
        assertEquals(AssignmentType.values().length, result.getAssignmentTypes().size());
        verify(courseService, times(1)).getCoursesByTeacher(teacher);
    }

    @Test
    void buildAssignmentsView_whenFormCreated_thenShouldHaveDefaultValues() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Blagoev")
                .email("ivan.blagoev@example.com")
                .userType(UserType.TEACHER)
                .build();

        Course course1 = Course.builder()
                .id(UUID.randomUUID())
                .name("Test Kurs")
                .teacher(teacher)
                .build();

        when(courseService.getCoursesByTeacher(teacher)).thenReturn(List.of(course1));

        TeacherAssignmentsViewDto result = teacherService.buildAssignmentsView(teacher);

        assertNotNull(result);
        assertNotNull(result.getForm());
        assertEquals("", result.getForm().getTitle());
        assertEquals("", result.getForm().getDescription());
        assertEquals(AssignmentType.values()[0], result.getForm().getAssignmentType());
        assertEquals(LocalDate.now(), result.getForm().getDueDate());
        verify(courseService, times(1)).getCoursesByTeacher(teacher);
    }

    @Test
    void buildAssignmentsView_whenAllAssignmentTypes_thenShouldIncludeAll() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Blagoev")
                .email("ivan.blagoev@example.com")
                .userType(UserType.TEACHER)
                .build();

        when(courseService.getCoursesByTeacher(teacher)).thenReturn(Collections.emptyList());

        TeacherAssignmentsViewDto result = teacherService.buildAssignmentsView(teacher);

        assertNotNull(result);
        assertNotNull(result.getAssignmentTypes());
        assertTrue(result.getAssignmentTypes().contains(AssignmentType.HOMEWORK));
        assertTrue(result.getAssignmentTypes().contains(AssignmentType.TEST_PREPARATION));
        assertTrue(result.getAssignmentTypes().contains(AssignmentType.OPTIONAL_WORK));
        verify(courseService, times(1)).getCoursesByTeacher(teacher);
    }
}



