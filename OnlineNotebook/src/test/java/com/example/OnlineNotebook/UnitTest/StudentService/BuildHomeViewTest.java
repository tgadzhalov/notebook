package com.example.OnlineNotebook.UnitTest.StudentService;

import com.example.OnlineNotebook.client.dto.AttendanceResponseDto;
import com.example.OnlineNotebook.client.AttendanceStatus;
import com.example.OnlineNotebook.client.service.AttendanceClientService;
import com.example.OnlineNotebook.models.dtos.student.home.StudentHomeViewDto;
import com.example.OnlineNotebook.models.entities.Assignment;
import com.example.OnlineNotebook.models.entities.Course;
import com.example.OnlineNotebook.models.entities.Grade;
import com.example.OnlineNotebook.models.entities.User;
import com.example.OnlineNotebook.models.enums.AssignmentType;
import com.example.OnlineNotebook.models.enums.GradeLetter;
import com.example.OnlineNotebook.models.enums.GradeType;
import com.example.OnlineNotebook.models.enums.SubjectType;
import com.example.OnlineNotebook.models.enums.UserType;
import com.example.OnlineNotebook.repositories.AssignmentRepository;
import com.example.OnlineNotebook.repositories.GradeRepository;
import com.example.OnlineNotebook.services.StudentService;
import com.example.OnlineNotebook.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuildHomeViewTest {

    @InjectMocks
    private StudentService studentService;
    @Mock
    private UserService userService;
    @Mock
    private GradeRepository gradeRepository;
    @Mock
    private AssignmentRepository assignmentRepository;
    @Mock
    private AttendanceClientService attendanceClientService;

    @Test
    void buildHomeView_whenStudentHasNoData_thenShouldReturnEmptyLists() {
        UUID studentId = UUID.randomUUID();
        User student = User.builder()
                .id(studentId)
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .userType(UserType.STUDENT)
                .course(null)
                .build();

        when(userService.getById(studentId)).thenReturn(student);
        when(gradeRepository.findByStudent(student)).thenReturn(Collections.emptyList());
        when(attendanceClientService.getAttendances(studentId, studentId))
                .thenReturn(ResponseEntity.ok(Collections.emptyList()));

        StudentHomeViewDto result = studentService.buildHomeView(studentId);

        assertNotNull(result);
        assertNotNull(result.getProfile());
        assertNotNull(result.getQuickStats());
        assertNotNull(result.getRecentGrades());
        assertTrue(result.getRecentGrades().isEmpty());
        assertNotNull(result.getUpcomingAssignments());
        assertTrue(result.getUpcomingAssignments().isEmpty());
        assertNotNull(result.getSubjectGrades());
        assertTrue(result.getSubjectGrades().isEmpty());
        assertNotNull(result.getLeaderboard());
        assertTrue(result.getLeaderboard().isEmpty());
        assertNotNull(result.getAttendances());
        assertTrue(result.getAttendances().isEmpty());
        verify(userService, times(1)).getById(studentId);
        verify(gradeRepository, times(1)).findByStudent(student);
        verify(assignmentRepository, never()).findByCourse(any());
        verify(attendanceClientService, times(1)).getAttendances(studentId, studentId);
    }

    @Test
    void buildHomeView_whenStudentHasCourseAndData_thenShouldReturnCompleteView() {
        UUID studentId = UUID.randomUUID();
        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("Mathematics")
                .build();

        User student = User.builder()
                .id(studentId)
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .userType(UserType.STUDENT)
                .course(course)
                .build();

        Grade grade = Grade.builder()
                .id(UUID.randomUUID())
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeLetter(GradeLetter.GOOD)
                .gradeType(GradeType.END_OF_YEAR_EXAM)
                .dateGraded(LocalDateTime.now())
                .gradedBy(User.builder().id(UUID.randomUUID()).build())
                .build();

        Assignment assignment = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Math Homework")
                .type(AssignmentType.HOMEWORK)
                .dueDate(LocalDateTime.now().plusDays(5))
                .assignedDate(LocalDateTime.now())
                .course(course)
                .createdBy(User.builder().id(UUID.randomUUID()).build())
                .build();

        AttendanceResponseDto attendance = AttendanceResponseDto.builder()
                .id(UUID.randomUUID())
                .studentId(studentId)
                .status(AttendanceStatus.ABSENT)
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.getById(studentId)).thenReturn(student);
        when(gradeRepository.findByStudent(student)).thenReturn(List.of(grade));
        when(assignmentRepository.findByCourse(course)).thenReturn(List.of(assignment));
        when(attendanceClientService.getAttendances(studentId, studentId))
                .thenReturn(ResponseEntity.ok(List.of(attendance)));

        StudentHomeViewDto result = studentService.buildHomeView(studentId);

        assertNotNull(result);
        assertNotNull(result.getProfile());
        assertEquals(studentId, result.getProfile().getId());
        assertNotNull(result.getQuickStats());
        assertNotNull(result.getRecentGrades());
        assertFalse(result.getRecentGrades().isEmpty());
        assertNotNull(result.getUpcomingAssignments());
        assertFalse(result.getUpcomingAssignments().isEmpty());
        assertNotNull(result.getSubjectGrades());
        assertFalse(result.getSubjectGrades().isEmpty());
        assertNotNull(result.getLeaderboard());
        assertNotNull(result.getAttendances());
        assertEquals(1, result.getAttendances().size());
        verify(userService, times(1)).getById(studentId);
        verify(gradeRepository, times(1)).findByStudent(student);
        verify(assignmentRepository, times(1)).findByCourse(course);
        verify(attendanceClientService, times(1)).getAttendances(studentId, studentId);
    }

    @Test
    void buildHomeView_whenAttendanceServiceThrowsException_thenShouldReturnEmptyAttendances() {
        UUID studentId = UUID.randomUUID();
        User student = User.builder()
                .id(studentId)
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .userType(UserType.STUDENT)
                .course(null)
                .build();

        when(userService.getById(studentId)).thenReturn(student);
        when(gradeRepository.findByStudent(student)).thenReturn(Collections.emptyList());
        when(attendanceClientService.getAttendances(studentId, studentId))
                .thenThrow(new RuntimeException("Service unavailable"));

        StudentHomeViewDto result = studentService.buildHomeView(studentId);

        assertNotNull(result);
        assertNotNull(result.getAttendances());
        assertTrue(result.getAttendances().isEmpty());
        verify(attendanceClientService, times(1)).getAttendances(studentId, studentId);
    }

    @Test
    void buildHomeView_whenAttendanceServiceReturnsNull_thenShouldReturnEmptyAttendances() {
        UUID studentId = UUID.randomUUID();
        User student = User.builder()
                .id(studentId)
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .userType(UserType.STUDENT)
                .course(null)
                .build();

        when(userService.getById(studentId)).thenReturn(student);
        when(gradeRepository.findByStudent(student)).thenReturn(Collections.emptyList());
        when(attendanceClientService.getAttendances(studentId, studentId))
                .thenReturn(null);

        StudentHomeViewDto result = studentService.buildHomeView(studentId);

        assertNotNull(result);
        assertNotNull(result.getAttendances());
        assertTrue(result.getAttendances().isEmpty());
        verify(attendanceClientService, times(1)).getAttendances(studentId, studentId);
    }

    @Test
    void buildHomeView_whenAttendanceServiceReturnsNullBody_thenShouldReturnEmptyAttendances() {
        UUID studentId = UUID.randomUUID();
        User student = User.builder()
                .id(studentId)
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .userType(UserType.STUDENT)
                .course(null)
                .build();

        when(userService.getById(studentId)).thenReturn(student);
        when(gradeRepository.findByStudent(student)).thenReturn(Collections.emptyList());
        when(attendanceClientService.getAttendances(studentId, studentId))
                .thenReturn(ResponseEntity.ok(null));

        StudentHomeViewDto result = studentService.buildHomeView(studentId);

        assertNotNull(result);
        assertNotNull(result.getAttendances());
        assertTrue(result.getAttendances().isEmpty());
        verify(attendanceClientService, times(1)).getAttendances(studentId, studentId);
    }
}
