package com.example.OnlineNotebook.UnitTest.TeacherService;

import com.example.OnlineNotebook.exceptions.ResourceNotFoundException;
import com.example.OnlineNotebook.models.dtos.teacher.student.TeacherStudentsViewDto;
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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BuildStudentsViewTest {

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
    void buildStudentsView_whenNoCourses_thenShouldReturnEmptyView() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Blagoev")
                .email("ivan.blagoev@example.com")
                .userType(UserType.TEACHER)
                .build();

        when(courseService.getCoursesByTeacher(teacher)).thenReturn(Collections.emptyList());

        TeacherStudentsViewDto result = teacherService.buildStudentsView(teacher, null, null);

        assertNotNull(result);
        assertNull(result.getSelectedCourseId());
        assertTrue(result.getCourses().isEmpty());
        assertTrue(result.getStudents().isEmpty());
        assertTrue(result.getAssignments().isEmpty());
        assertFalse(result.isShowGradesModal());
        assertNull(result.getSelectedStudentId());
        verify(courseService, times(1)).getCoursesByTeacher(teacher);
        verify(userService, never()).getStudentsByCourse(any());
    }

    @Test
    void buildStudentsView_whenCourseIdProvided_thenShouldSelectThatCourse() {
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

        TeacherStudentsViewDto result = teacherService.buildStudentsView(teacher, course2.getId(), null);

        assertNotNull(result);
        assertEquals(course2.getId(), result.getSelectedCourseId());
        assertEquals(2, result.getCourses().size());
        verify(courseService, times(1)).getCoursesByTeacher(teacher);
    }

    @Test
    void buildStudentsView_whenCourseIdNotFound_thenShouldSelectFirstCourse() {
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

        UUID nonExistentId = UUID.randomUUID();
        when(courseService.getCoursesByTeacher(teacher)).thenReturn(List.of(course1));

        TeacherStudentsViewDto result = teacherService.buildStudentsView(teacher, nonExistentId, null);

        assertNotNull(result);
        assertEquals(course1.getId(), result.getSelectedCourseId());
        verify(courseService, times(1)).getCoursesByTeacher(teacher);
    }

    @Test
    void buildStudentsView_whenNoCourseIdProvided_thenShouldSelectFirstCourse() {
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

        User student1 = User.builder()
                .id(UUID.randomUUID())
                .firstName("Petar")
                .lastName("Petrov")
                .email("petar.petrov@example.com")
                .userType(UserType.STUDENT)
                .course(course1)
                .build();

        when(courseService.getCoursesByTeacher(teacher)).thenReturn(List.of(course1));
        when(userService.getStudentsByCourse(course1)).thenReturn(List.of(student1));
        when(assignmentRepository.findByCourse(course1)).thenReturn(Collections.emptyList());

        TeacherStudentsViewDto result = teacherService.buildStudentsView(teacher, null, null);

        assertNotNull(result);
        assertEquals(course1.getId(), result.getSelectedCourseId());
        assertEquals(1, result.getStudents().size());
        assertEquals(student1.getId(), result.getStudents().get(0).getId());
        verify(courseService, times(1)).getCoursesByTeacher(teacher);
        verify(userService, times(1)).getStudentsByCourse(course1);
    }

    @Test
    void buildStudentsView_whenStudentIdProvided_thenShouldShowGradesModal() {
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

        User student1 = User.builder()
                .id(UUID.randomUUID())
                .firstName("Petar")
                .lastName("Petrov")
                .email("petar.petrov@example.com")
                .userType(UserType.STUDENT)
                .course(course1)
                .build();

        when(courseService.getCoursesByTeacher(teacher)).thenReturn(List.of(course1));
        when(userService.getStudentsByCourse(course1)).thenReturn(List.of(student1));
        when(assignmentRepository.findByCourse(course1)).thenReturn(Collections.emptyList());
        when(gradeService.getStudentGradesForTeacher(teacher, student1.getId(), course1.getId()))
                .thenReturn(Collections.emptyList());

        TeacherStudentsViewDto result = teacherService.buildStudentsView(teacher, course1.getId(), student1.getId());

        assertNotNull(result);
        assertTrue(result.isShowGradesModal());
        assertEquals(student1.getId(), result.getSelectedStudentId());
        assertEquals(student1.getId(), result.getSelectedStudent().getId());
        verify(gradeService, times(1)).getStudentGradesForTeacher(teacher, student1.getId(), course1.getId());
    }

    @Test
    void buildStudentsView_whenStudentIdNotFound_thenShouldShowError() {
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

        User student1 = User.builder()
                .id(UUID.randomUUID())
                .firstName("Petar")
                .lastName("Petrov")
                .email("petar.petrov@example.com")
                .userType(UserType.STUDENT)
                .course(course1)
                .build();

        UUID nonExistentStudentId = UUID.randomUUID();

        when(courseService.getCoursesByTeacher(teacher)).thenReturn(List.of(course1));
        when(userService.getStudentsByCourse(course1)).thenReturn(List.of(student1));
        when(assignmentRepository.findByCourse(course1)).thenReturn(Collections.emptyList());

        TeacherStudentsViewDto result = teacherService.buildStudentsView(teacher, course1.getId(), nonExistentStudentId);

        assertNotNull(result);
        assertNull(result.getSelectedStudent());
        assertTrue(result.getStudentGrades().isEmpty());
        verify(gradeService, never()).getStudentGradesForTeacher(any(), any(), any());
    }

    @Test
    void buildStudentsView_whenGradeServiceThrowsException_thenShouldShowError() {
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

        User student1 = User.builder()
                .id(UUID.randomUUID())
                .firstName("Petar")
                .lastName("Petrov")
                .email("petar.petrov@example.com")
                .userType(UserType.STUDENT)
                .course(course1)
                .build();

        when(courseService.getCoursesByTeacher(teacher)).thenReturn(List.of(course1));
        when(userService.getStudentsByCourse(course1)).thenReturn(List.of(student1));
        when(assignmentRepository.findByCourse(course1)).thenReturn(Collections.emptyList());
        when(gradeService.getStudentGradesForTeacher(teacher, student1.getId(), course1.getId()))
                .thenThrow(new ResourceNotFoundException("Student not found"));

        TeacherStudentsViewDto result = teacherService.buildStudentsView(teacher, course1.getId(), student1.getId());

        assertNotNull(result);
        assertTrue(result.isShowGradesModal());
        assertNotNull(result.getGradeErrorMessage());
        assertTrue(result.getStudentGrades().isEmpty());
        verify(gradeService, times(1)).getStudentGradesForTeacher(teacher, student1.getId(), course1.getId());
    }

    @Test
    void buildStudentsView_whenAssignmentsExist_thenShouldIncludeThem() {
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

        Assignment assignment1 = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Zadanie 1")
                .type(AssignmentType.HOMEWORK)
                .course(course1)
                .createdBy(teacher)
                .dueDate(LocalDateTime.now().plusDays(7))
                .assignedDate(LocalDateTime.now())
                .build();

        when(courseService.getCoursesByTeacher(teacher)).thenReturn(List.of(course1));
        when(userService.getStudentsByCourse(course1)).thenReturn(Collections.emptyList());
        when(assignmentRepository.findByCourse(course1)).thenReturn(List.of(assignment1));

        TeacherStudentsViewDto result = teacherService.buildStudentsView(teacher, course1.getId(), null);

        assertNotNull(result);
        assertEquals(1, result.getAssignments().size());
        assertEquals(assignment1.getId(), result.getAssignments().get(0).getId());
        verify(assignmentRepository, times(1)).findByCourse(course1);
    }
}

