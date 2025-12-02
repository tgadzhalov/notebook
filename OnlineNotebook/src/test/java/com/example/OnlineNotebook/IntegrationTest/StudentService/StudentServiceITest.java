package com.example.OnlineNotebook.IntegrationTest.StudentService;

import com.example.OnlineNotebook.client.dto.AttendanceResponseDto;
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
import com.example.OnlineNotebook.repositories.CourseRepository;
import com.example.OnlineNotebook.repositories.GradeRepository;
import com.example.OnlineNotebook.repositories.UserRepository;
import com.example.OnlineNotebook.services.StudentService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class StudentServiceITest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    @MockBean
    private AttendanceClientService attendanceClientService;

    private User student;
    private Course course;
    private User teacher;

    @BeforeEach
    void setUp() {
        teacher = User.builder()
                .firstName("Ivan")
                .lastName("Blagoev")
                .email("ivan.blagoev@example.com")
                .password(passwordEncoder.encode("password123"))
                .userType(UserType.TEACHER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        teacher = userRepository.save(teacher);

        course = Course.builder()
                .name("Test Kurs")
                .description("Opisanie")
                .schoolYear("2024-2025")
                .teacher(teacher)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        course.setSubjects(Arrays.asList(SubjectType.MATH, SubjectType.SCIENCE));
        course = courseRepository.save(course);

        student = User.builder()
                .firstName("Petar")
                .lastName("Petrov")
                .email("petar.petrov@example.com")
                .password(passwordEncoder.encode("password123"))
                .userType(UserType.STUDENT)
                .studentClass("10A")
                .course(course)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        student = userRepository.save(student);
        entityManager.flush();
        entityManager.clear();

        when(attendanceClientService.getAttendances(any(UUID.class), any(UUID.class)))
                .thenReturn(ResponseEntity.ok(List.of()));
    }

    @Test
    void buildHomeView_whenValidStudent_thenShouldReturnHomeView() {
        StudentHomeViewDto result = studentService.buildHomeView(student.getId());
        entityManager.flush();
        entityManager.clear();

        assertNotNull(result);
        assertNotNull(result.getProfile());
        assertEquals(student.getId(), result.getProfile().getId());
        assertEquals("Petar", result.getProfile().getFirstName());
        assertEquals("Petrov", result.getProfile().getLastName());
        assertEquals("Test Kurs", result.getProfile().getCourseName());
        assertNotNull(result.getQuickStats());
        assertNotNull(result.getRecentGrades());
        assertNotNull(result.getUpcomingAssignments());
        assertNotNull(result.getSubjectGrades());
        assertNotNull(result.getLeaderboard());
    }

    @Test
    void buildHomeView_whenStudentHasGrades_thenShouldIncludeGrades() {
        Grade grade1 = Grade.builder()
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now())
                .build();
        grade1 = gradeRepository.save(grade1);

        Grade grade2 = Grade.builder()
                .student(student)
                .subjectType(SubjectType.SCIENCE)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.VERY_GOOD)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now().minusDays(1))
                .build();
        grade2 = gradeRepository.save(grade2);
        entityManager.flush();
        entityManager.clear();

        student = userRepository.findById(student.getId()).orElse(null);
        StudentHomeViewDto result = studentService.buildHomeView(student.getId());

        assertNotNull(result);
        assertNotNull(result.getRecentGrades());
        assertTrue(result.getRecentGrades().size() >= 2);
        assertNotNull(result.getSubjectGrades());
        assertTrue(result.getSubjectGrades().size() >= 2);
    }

    @Test
    void buildHomeView_whenStudentHasAssignments_thenShouldIncludeAssignments() {
        Assignment assignment1 = Assignment.builder()
                .title("Zadanie 1")
                .description("Opisanie na zadanie")
                .type(AssignmentType.HOMEWORK)
                .course(course)
                .createdBy(teacher)
                .dueDate(LocalDateTime.now().plusDays(7))
                .assignedDate(LocalDateTime.now())
                .build();
        assignment1 = assignmentRepository.save(assignment1);

        Assignment assignment2 = Assignment.builder()
                .title("Zadanie 2")
                .description("Opisanie na zadanie 2")
                .type(AssignmentType.TEST_PREPARATION)
                .course(course)
                .createdBy(teacher)
                .dueDate(LocalDateTime.now().plusDays(14))
                .assignedDate(LocalDateTime.now())
                .build();
        assignment2 = assignmentRepository.save(assignment2);
        entityManager.flush();
        entityManager.clear();

        student = userRepository.findById(student.getId()).orElse(null);
        StudentHomeViewDto result = studentService.buildHomeView(student.getId());

        assertNotNull(result);
        assertNotNull(result.getUpcomingAssignments());
        assertTrue(result.getUpcomingAssignments().size() >= 2);
    }

    @Test
    void buildHomeView_whenStudentHasNoGrades_thenShouldShowEmptyGrades() {
        StudentHomeViewDto result = studentService.buildHomeView(student.getId());

        assertNotNull(result);
        assertNotNull(result.getRecentGrades());
        assertTrue(result.getRecentGrades().isEmpty());
        assertNotNull(result.getSubjectGrades());
        assertTrue(result.getSubjectGrades().isEmpty());
    }

    @Test
    void buildHomeView_whenStudentHasNoAssignments_thenShouldShowEmptyAssignments() {
        StudentHomeViewDto result = studentService.buildHomeView(student.getId());

        assertNotNull(result);
        assertNotNull(result.getUpcomingAssignments());
        assertTrue(result.getUpcomingAssignments().isEmpty());
    }

    @Test
    void buildHomeView_whenAttendanceServiceReturnsData_thenShouldIncludeAttendances() {
        AttendanceResponseDto attendance1 = AttendanceResponseDto.builder()
                .id(UUID.randomUUID())
                .status(com.example.OnlineNotebook.client.AttendanceStatus.ABSENT)
                .build();

        AttendanceResponseDto attendance2 = AttendanceResponseDto.builder()
                .id(UUID.randomUUID())
                .status(com.example.OnlineNotebook.client.AttendanceStatus.LATE)
                .build();

        when(attendanceClientService.getAttendances(any(UUID.class), any(UUID.class)))
                .thenReturn(ResponseEntity.ok(Arrays.asList(attendance1, attendance2)));

        StudentHomeViewDto result = studentService.buildHomeView(student.getId());

        assertNotNull(result);
        assertNotNull(result.getAttendances());
        assertEquals(2, result.getAttendances().size());
    }

    @Test
    void buildHomeView_whenAttendanceServiceThrowsException_thenShouldHandleGracefully() {
        when(attendanceClientService.getAttendances(any(UUID.class), any(UUID.class)))
                .thenThrow(new RuntimeException("Service unavailable"));

        StudentHomeViewDto result = studentService.buildHomeView(student.getId());

        assertNotNull(result);
        assertNotNull(result.getAttendances());
        assertTrue(result.getAttendances().isEmpty());
    }

    @Test
    void buildHomeView_whenStudentHasMultipleClassmates_thenShouldBuildLeaderboard() {
        User student2 = User.builder()
                .firstName("Georgi")
                .lastName("Georgiev")
                .email("georgi.georgiev@example.com")
                .password(passwordEncoder.encode("password123"))
                .userType(UserType.STUDENT)
                .studentClass("10A")
                .course(course)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        student2 = userRepository.save(student2);

        Grade grade2 = Grade.builder()
                .student(student2)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.EXCELLENT)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now())
                .build();
        grade2 = gradeRepository.save(grade2);
        entityManager.flush();
        entityManager.clear();

        student = userRepository.findById(student.getId()).orElse(null);
        StudentHomeViewDto result = studentService.buildHomeView(student.getId());

        assertNotNull(result);
        assertNotNull(result.getLeaderboard());
        assertTrue(result.getLeaderboard().size() >= 1);
    }

    @Test
    void buildHomeView_whenStudentHasNoCourse_thenShouldHandleGracefully() {
        User studentWithoutCourse = User.builder()
                .firstName("Nikolay")
                .lastName("Nikolov")
                .email("nikolay.nikolov@example.com")
                .password(passwordEncoder.encode("password123"))
                .userType(UserType.STUDENT)
                .studentClass("10B")
                .course(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        studentWithoutCourse = userRepository.save(studentWithoutCourse);
        entityManager.flush();
        entityManager.clear();

        studentWithoutCourse = userRepository.findById(studentWithoutCourse.getId()).orElse(null);
        StudentHomeViewDto result = studentService.buildHomeView(studentWithoutCourse.getId());

        assertNotNull(result);
        assertNull(result.getProfile().getCourseName());
        assertNotNull(result.getLeaderboard());
        assertTrue(result.getLeaderboard().isEmpty());
    }
}

