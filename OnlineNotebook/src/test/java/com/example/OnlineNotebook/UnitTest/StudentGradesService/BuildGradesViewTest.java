package com.example.OnlineNotebook.UnitTest.StudentGradesService;

import com.example.OnlineNotebook.exceptions.ResourceNotFoundException;
import com.example.OnlineNotebook.models.dtos.student.grades.StudentGradesViewDto;
import com.example.OnlineNotebook.models.entities.Course;
import com.example.OnlineNotebook.models.entities.Grade;
import com.example.OnlineNotebook.models.entities.User;
import com.example.OnlineNotebook.models.enums.GradeLetter;
import com.example.OnlineNotebook.models.enums.GradeType;
import com.example.OnlineNotebook.models.enums.SubjectType;
import com.example.OnlineNotebook.models.enums.UserType;
import com.example.OnlineNotebook.repositories.GradeRepository;
import com.example.OnlineNotebook.services.StudentGradesService;
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
class BuildGradesViewTest {

    @InjectMocks
    private StudentGradesService studentGradesService;

    @Mock
    private UserService userService;

    @Mock
    private GradeRepository gradeRepository;

    @Test
    void buildGradesView_whenStudentExists_thenReturnGradesViewDto() {
        UUID studentId = UUID.randomUUID();
        
        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("10A")
                .build();
        
        User student = User.builder()
                .id(studentId)
                .firstName("Ivan")
                .lastName("Schweicarov")
                .email("ivan@example.com")
                .studentClass("10A")
                .course(course)
                .userType(UserType.STUDENT)
                .build();
        
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("teacher@example.com")
                .userType(UserType.TEACHER)
                .build();
        
        Grade grade1 = Grade.builder()
                .id(UUID.randomUUID())
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now())
                .feedback("Good work")
                .build();
        
        Grade grade2 = Grade.builder()
                .id(UUID.randomUUID())
                .student(student)
                .subjectType(SubjectType.ENGLISH)
                .gradeType(GradeType.PROJECT)
                .gradeLetter(GradeLetter.EXCELLENT)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now())
                .build();
        
        List<Grade> grades = List.of(grade1, grade2);
        
        when(userService.getById(studentId)).thenReturn(student);
        when(gradeRepository.findByStudent(student)).thenReturn(grades);
        
        StudentGradesViewDto result = studentGradesService.buildGradesView(studentId);
        
        assertNotNull(result);
        assertNotNull(result.getProfile());
        assertNotNull(result.getSummary());
        assertNotNull(result.getSubjects());
        assertEquals(studentId, result.getProfile().getId());
        assertEquals("Ivan", result.getProfile().getFirstName());
        assertEquals("Schweicarov", result.getProfile().getLastName());
        assertEquals("10A", result.getProfile().getStudentClass());
        assertEquals("10A", result.getProfile().getCourseName());
        assertEquals(2, result.getSummary().getTotalGrades());
        assertEquals(2, result.getSummary().getSubjectsCount());
        assertEquals(2, result.getSubjects().size());
        
        verify(userService, times(1)).getById(studentId);
        verify(gradeRepository, times(1)).findByStudent(student);
    }

    @Test
    void buildGradesView_whenStudentHasNoGrades_thenReturnEmptyGradesView() {
        UUID studentId = UUID.randomUUID();
        
        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("10A")
                .build();
        
        User student = User.builder()
                .id(studentId)
                .firstName("Ivan")
                .lastName("Schweicarov")
                .email("ivan@example.com")
                .studentClass("10A")
                .course(course)
                .userType(UserType.STUDENT)
                .build();
        
        when(userService.getById(studentId)).thenReturn(student);
        when(gradeRepository.findByStudent(student)).thenReturn(Collections.emptyList());
        
        StudentGradesViewDto result = studentGradesService.buildGradesView(studentId);
        
        assertNotNull(result);
        assertNotNull(result.getProfile());
        assertNotNull(result.getSummary());
        assertNotNull(result.getSubjects());
        assertEquals(0, result.getSummary().getTotalGrades());
        assertEquals(0, result.getSummary().getSubjectsCount());
        assertEquals(0, result.getSubjects().size());
        assertEquals("--", result.getSummary().getOverallAverageDisplay());
        
        verify(userService, times(1)).getById(studentId);
        verify(gradeRepository, times(1)).findByStudent(student);
    }

    @Test
    void buildGradesView_whenStudentNotFound_thenThrowResourceNotFoundException() {
        UUID studentId = UUID.randomUUID();
        
        when(userService.getById(studentId)).thenThrow(new ResourceNotFoundException("User not found"));
        
        assertThrows(ResourceNotFoundException.class, () -> {
            studentGradesService.buildGradesView(studentId);
        });
        
        verify(userService, times(1)).getById(studentId);
        verify(gradeRepository, never()).findByStudent(any());
    }

    @Test
    void buildGradesView_whenStudentHasNullCourse_thenHandleGracefully() {
        UUID studentId = UUID.randomUUID();
        
        User student = User.builder()
                .id(studentId)
                .firstName("Ivan")
                .lastName("Schweicarov")
                .email("ivan@example.com")
                .studentClass("10A")
                .course(null)
                .userType(UserType.STUDENT)
                .build();
        
        when(userService.getById(studentId)).thenReturn(student);
        when(gradeRepository.findByStudent(student)).thenReturn(Collections.emptyList());
        
        StudentGradesViewDto result = studentGradesService.buildGradesView(studentId);
        
        assertNotNull(result);
        assertNotNull(result.getProfile());
        assertNull(result.getProfile().getCourseName());
        
        verify(userService, times(1)).getById(studentId);
        verify(gradeRepository, times(1)).findByStudent(student);
    }
}

