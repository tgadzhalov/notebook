package com.example.OnlineNotebook.UnitTest.StudentHomeService;

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
import com.example.OnlineNotebook.services.StudentHomeService;
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
class BuildHomeViewTest {

    @InjectMocks
    private StudentHomeService studentHomeService;
    @Mock
    private UserService userService;
    @Mock
    private GradeRepository gradeRepository;
    @Mock
    private AssignmentRepository assignmentRepository;

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

        StudentHomeViewDto result = studentHomeService.buildHomeView(studentId);

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
        verify(userService, times(1)).getById(studentId);
        verify(gradeRepository, times(1)).findByStudent(student);
        verify(assignmentRepository, never()).findByCourse(any());
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
                .studentClass("10A")
                .build();

        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .userType(UserType.TEACHER)
                .build();

        Grade grade = Grade.builder()
                .id(UUID.randomUUID())
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeLetter(GradeLetter.GOOD)
                .gradeType(GradeType.END_OF_YEAR_EXAM)
                .dateGraded(LocalDateTime.now())
                .gradedBy(teacher)
                .build();

        Assignment assignment = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Math Homework")
                .type(AssignmentType.HOMEWORK)
                .dueDate(LocalDateTime.now().plusDays(5))
                .assignedDate(LocalDateTime.now())
                .course(course)
                .createdBy(teacher)
                .build();

        when(userService.getById(studentId)).thenReturn(student);
        when(gradeRepository.findByStudent(student)).thenReturn(List.of(grade));
        when(assignmentRepository.findByCourse(course)).thenReturn(List.of(assignment));
        when(userService.getStudentsByCourse(course)).thenReturn(List.of(student));

        StudentHomeViewDto result = studentHomeService.buildHomeView(studentId);

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
        verify(userService, times(1)).getById(studentId);
        verify(gradeRepository, times(2)).findByStudent(student);
        verify(assignmentRepository, times(1)).findByCourse(course);
        verify(userService, times(1)).getStudentsByCourse(course);
    }

    @Test
    void buildHomeView_whenStudentHasNullCourse_thenShouldReturnEmptyAssignments() {
        UUID studentId = UUID.randomUUID();
        User student = User.builder()
                .id(studentId)
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .userType(UserType.STUDENT)
                .course(null)
                .build();

        Grade grade = Grade.builder()
                .id(UUID.randomUUID())
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeLetter(GradeLetter.GOOD)
                .dateGraded(LocalDateTime.now())
                .gradedBy(User.builder().id(UUID.randomUUID()).build())
                .build();

        when(userService.getById(studentId)).thenReturn(student);
        when(gradeRepository.findByStudent(student)).thenReturn(List.of(grade));

        StudentHomeViewDto result = studentHomeService.buildHomeView(studentId);

        assertNotNull(result);
        assertNotNull(result.getUpcomingAssignments());
        assertTrue(result.getUpcomingAssignments().isEmpty());
        assertNotNull(result.getLeaderboard());
        assertTrue(result.getLeaderboard().isEmpty());
        verify(assignmentRepository, never()).findByCourse(any());
        verify(userService, never()).getStudentsByCourse(any());
    }

    @Test
    void buildHomeView_whenStudentHasMultipleGrades_thenShouldReturnSortedRecentGrades() {
        UUID studentId = UUID.randomUUID();
        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("Mathematics")
                .build();

        User student = User.builder()
                .id(studentId)
                .firstName("Alice")
                .lastName("Smith")
                .userType(UserType.STUDENT)
                .course(course)
                .build();

        LocalDateTime now = LocalDateTime.now();
        Grade grade1 = Grade.builder()
                .id(UUID.randomUUID())
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeLetter(GradeLetter.GOOD)
                .dateGraded(now.minusDays(1))
                .gradedBy(User.builder().id(UUID.randomUUID()).build())
                .build();

        Grade grade2 = Grade.builder()
                .id(UUID.randomUUID())
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeLetter(GradeLetter.VERY_GOOD)
                .dateGraded(now.minusDays(3))
                .gradedBy(User.builder().id(UUID.randomUUID()).build())
                .build();

        Grade grade3 = Grade.builder()
                .id(UUID.randomUUID())
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeLetter(GradeLetter.EXCELLENT)
                .dateGraded(now)
                .gradedBy(User.builder().id(UUID.randomUUID()).build())
                .build();

        Grade grade4 = Grade.builder()
                .id(UUID.randomUUID())
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeLetter(GradeLetter.AVERAGE)
                .dateGraded(now.minusDays(2))
                .gradedBy(User.builder().id(UUID.randomUUID()).build())
                .build();

        when(userService.getById(studentId)).thenReturn(student);
        when(gradeRepository.findByStudent(student)).thenReturn(List.of(grade1, grade2, grade3, grade4));
        when(assignmentRepository.findByCourse(course)).thenReturn(Collections.emptyList());
        when(userService.getStudentsByCourse(course)).thenReturn(List.of(student));

        StudentHomeViewDto result = studentHomeService.buildHomeView(studentId);

        assertNotNull(result);
        assertNotNull(result.getRecentGrades());
        assertEquals(3, result.getRecentGrades().size());
        assertEquals(GradeLetter.EXCELLENT.name(), result.getRecentGrades().get(0).getGradeLetter());
        assertEquals(GradeLetter.GOOD.name(), result.getRecentGrades().get(1).getGradeLetter());
        assertEquals(GradeLetter.AVERAGE.name(), result.getRecentGrades().get(2).getGradeLetter());
        verify(gradeRepository, times(2)).findByStudent(student);
    }

    @Test
    void buildHomeView_whenStudentHasMultipleAssignments_thenShouldReturnSortedUpcomingAssignments() {
        UUID studentId = UUID.randomUUID();
        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("Mathematics")
                .build();

        User student = User.builder()
                .id(studentId)
                .firstName("Alice")
                .lastName("Smith")
                .userType(UserType.STUDENT)
                .course(course)
                .build();

        User teacher = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.TEACHER)
                .build();

        LocalDateTime now = LocalDateTime.now();
        Assignment assignment1 = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Assignment 1")
                .type(AssignmentType.HOMEWORK)
                .dueDate(now.plusDays(10))
                .assignedDate(now)
                .course(course)
                .createdBy(teacher)
                .build();

        Assignment assignment2 = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Assignment 2")
                .type(AssignmentType.HOMEWORK)
                .dueDate(now.plusDays(3))
                .assignedDate(now)
                .course(course)
                .createdBy(teacher)
                .build();

        Assignment assignment3 = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Assignment 3")
                .type(AssignmentType.HOMEWORK)
                .dueDate(now.plusDays(5))
                .assignedDate(now)
                .course(course)
                .createdBy(teacher)
                .build();

        Assignment pastAssignment = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Past Assignment")
                .type(AssignmentType.HOMEWORK)
                .dueDate(now.minusDays(1))
                .assignedDate(now.minusDays(5))
                .course(course)
                .createdBy(teacher)
                .build();

        when(userService.getById(studentId)).thenReturn(student);
        when(gradeRepository.findByStudent(student)).thenReturn(Collections.emptyList());
        when(assignmentRepository.findByCourse(course)).thenReturn(List.of(assignment1, assignment2, assignment3, pastAssignment));
        when(userService.getStudentsByCourse(course)).thenReturn(List.of(student));

        StudentHomeViewDto result = studentHomeService.buildHomeView(studentId);

        assertNotNull(result);
        assertNotNull(result.getUpcomingAssignments());
        assertEquals(3, result.getUpcomingAssignments().size());
        assertEquals("Assignment 2", result.getUpcomingAssignments().get(0).getTitle());
        assertEquals("Assignment 3", result.getUpcomingAssignments().get(1).getTitle());
        assertEquals("Assignment 1", result.getUpcomingAssignments().get(2).getTitle());
        verify(gradeRepository, times(2)).findByStudent(student);
    }

    @Test
    void buildHomeView_whenStudentHasGradesWithNullValues_thenShouldHandleGracefully() {
        UUID studentId = UUID.randomUUID();
        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("Mathematics")
                .build();

        User student = User.builder()
                .id(studentId)
                .firstName("Alice")
                .lastName("Smith")
                .userType(UserType.STUDENT)
                .course(course)
                .build();

        Grade gradeWithNulls = Grade.builder()
                .id(UUID.randomUUID())
                .student(student)
                .subjectType(null)
                .gradeLetter(null)
                .gradeType(null)
                .dateGraded(LocalDateTime.now())
                .gradedBy(User.builder().id(UUID.randomUUID()).build())
                .build();

        when(userService.getById(studentId)).thenReturn(student);
        when(gradeRepository.findByStudent(student)).thenReturn(List.of(gradeWithNulls));
        when(assignmentRepository.findByCourse(course)).thenReturn(Collections.emptyList());
        when(userService.getStudentsByCourse(course)).thenReturn(List.of(student));

        StudentHomeViewDto result = studentHomeService.buildHomeView(studentId);

        assertNotNull(result);
        assertNotNull(result.getRecentGrades());
        assertNotNull(result.getSubjectGrades());
        assertTrue(result.getSubjectGrades().isEmpty());
        verify(gradeRepository, times(2)).findByStudent(student);
    }

    @Test
    void buildHomeView_whenLeaderboardHasMultipleStudents_thenShouldReturnTopFive() {
        UUID studentId = UUID.randomUUID();
        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("Mathematics")
                .build();

        User student1 = User.builder()
                .id(studentId)
                .firstName("Alice")
                .lastName("Smith")
                .userType(UserType.STUDENT)
                .course(course)
                .studentClass("10A")
                .build();

        User student2 = User.builder()
                .id(UUID.randomUUID())
                .firstName("Bob")
                .lastName("Jones")
                .userType(UserType.STUDENT)
                .course(course)
                .studentClass("10A")
                .build();

        User student3 = User.builder()
                .id(UUID.randomUUID())
                .firstName("Charlie")
                .lastName("Brown")
                .userType(UserType.STUDENT)
                .course(course)
                .studentClass("10A")
                .build();

        User student4 = User.builder()
                .id(UUID.randomUUID())
                .firstName("Diana")
                .lastName("White")
                .userType(UserType.STUDENT)
                .course(course)
                .studentClass("10A")
                .build();

        User student5 = User.builder()
                .id(UUID.randomUUID())
                .firstName("Eve")
                .lastName("Black")
                .userType(UserType.STUDENT)
                .course(course)
                .studentClass("10A")
                .build();

        User student6 = User.builder()
                .id(UUID.randomUUID())
                .firstName("Frank")
                .lastName("Green")
                .userType(UserType.STUDENT)
                .course(course)
                .studentClass("10A")
                .build();

        Assignment assignment = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Test Assignment")
                .type(AssignmentType.HOMEWORK)
                .dueDate(LocalDateTime.now().plusDays(5))
                .assignedDate(LocalDateTime.now())
                .course(course)
                .createdBy(User.builder().id(UUID.randomUUID()).build())
                .build();

        when(userService.getById(studentId)).thenReturn(student1);
        when(gradeRepository.findByStudent(student1)).thenReturn(Collections.emptyList());
        when(gradeRepository.findByStudent(student2)).thenReturn(Collections.emptyList());
        when(gradeRepository.findByStudent(student3)).thenReturn(Collections.emptyList());
        when(gradeRepository.findByStudent(student4)).thenReturn(Collections.emptyList());
        when(gradeRepository.findByStudent(student5)).thenReturn(Collections.emptyList());
        when(gradeRepository.findByStudent(student6)).thenReturn(Collections.emptyList());
        when(assignmentRepository.findByCourse(course)).thenReturn(List.of(assignment));
        when(userService.getStudentsByCourse(course)).thenReturn(List.of(student1, student2, student3, student4, student5, student6));

        StudentHomeViewDto result = studentHomeService.buildHomeView(studentId);

        assertNotNull(result);
        assertNotNull(result.getLeaderboard());
        assertEquals(5, result.getLeaderboard().size());
        verify(gradeRepository, times(2)).findByStudent(student1);
        verify(gradeRepository, times(1)).findByStudent(student2);
        verify(gradeRepository, times(1)).findByStudent(student3);
        verify(gradeRepository, times(1)).findByStudent(student4);
        verify(gradeRepository, times(1)).findByStudent(student5);
        verify(gradeRepository, times(1)).findByStudent(student6);
    }

    @Test
    void buildHomeView_whenStudentHasNullId_thenShouldHandleGracefully() {
        UUID studentId = UUID.randomUUID();
        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("Mathematics")
                .build();

        User student = User.builder()
                .id(null)
                .firstName("Alice")
                .lastName("Smith")
                .userType(UserType.STUDENT)
                .course(course)
                .build();

        when(userService.getById(studentId)).thenReturn(student);
        when(gradeRepository.findByStudent(student)).thenReturn(Collections.emptyList());
        when(assignmentRepository.findByCourse(course)).thenReturn(Collections.emptyList());
        when(userService.getStudentsByCourse(course)).thenReturn(Collections.emptyList());

        StudentHomeViewDto result = studentHomeService.buildHomeView(studentId);

        assertNotNull(result);
        assertNotNull(result.getProfile());
        assertTrue(result.getProfile().getDisplayId().contains("--"));
        verify(gradeRepository, times(1)).findByStudent(student);
    }

    @Test
    void buildHomeView_whenStudentHasGradesWithNullGradeLetter_thenShouldFilterOut() {
        UUID studentId = UUID.randomUUID();
        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("Mathematics")
                .build();

        User student = User.builder()
                .id(studentId)
                .firstName("Alice")
                .lastName("Smith")
                .userType(UserType.STUDENT)
                .course(course)
                .build();

        Grade gradeWithNull = Grade.builder()
                .id(UUID.randomUUID())
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeLetter(null)
                .dateGraded(LocalDateTime.now())
                .gradedBy(User.builder().id(UUID.randomUUID()).build())
                .build();

        when(userService.getById(studentId)).thenReturn(student);
        when(gradeRepository.findByStudent(student)).thenReturn(List.of(gradeWithNull));
        when(assignmentRepository.findByCourse(course)).thenReturn(Collections.emptyList());
        when(userService.getStudentsByCourse(course)).thenReturn(List.of(student));

        StudentHomeViewDto result = studentHomeService.buildHomeView(studentId);

        assertNotNull(result);
        assertNotNull(result.getQuickStats());
        assertEquals("--", result.getQuickStats().getAverageGradeDisplay());
        verify(gradeRepository, times(2)).findByStudent(student);
    }

    @Test
    void buildHomeView_whenStudentHasMultipleSubjects_thenShouldReturnSortedByPercentage() {
        UUID studentId = UUID.randomUUID();
        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("Mathematics")
                .build();

        User student = User.builder()
                .id(studentId)
                .firstName("Alice")
                .lastName("Smith")
                .userType(UserType.STUDENT)
                .course(course)
                .build();

        User teacher = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.TEACHER)
                .build();

        Grade mathGrade1 = Grade.builder()
                .id(UUID.randomUUID())
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeLetter(GradeLetter.EXCELLENT)
                .dateGraded(LocalDateTime.now())
                .gradedBy(teacher)
                .build();

        Grade mathGrade2 = Grade.builder()
                .id(UUID.randomUUID())
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeLetter(GradeLetter.VERY_GOOD)
                .dateGraded(LocalDateTime.now())
                .gradedBy(teacher)
                .build();

        Grade physicsGrade = Grade.builder()
                .id(UUID.randomUUID())
                .student(student)
                .subjectType(SubjectType.BULGARIAN)
                .gradeLetter(GradeLetter.GOOD)
                .dateGraded(LocalDateTime.now())
                .gradedBy(teacher)
                .build();

        when(userService.getById(studentId)).thenReturn(student);
        when(gradeRepository.findByStudent(student)).thenReturn(List.of(mathGrade1, mathGrade2, physicsGrade));
        when(assignmentRepository.findByCourse(course)).thenReturn(Collections.emptyList());
        when(userService.getStudentsByCourse(course)).thenReturn(List.of(student));

        StudentHomeViewDto result = studentHomeService.buildHomeView(studentId);

        assertNotNull(result);
        assertNotNull(result.getSubjectGrades());
        assertEquals(2, result.getSubjectGrades().size());
        assertTrue(result.getSubjectGrades().get(0).getPercentage() >= result.getSubjectGrades().get(1).getPercentage());
        verify(gradeRepository, times(2)).findByStudent(student);
    }

    @Test
    void buildHomeView_whenAssignmentDueDateIsExactlyNow_thenShouldInclude() {
        UUID studentId = UUID.randomUUID();
        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("Mathematics")
                .build();

        User student = User.builder()
                .id(studentId)
                .firstName("Alice")
                .lastName("Smith")
                .userType(UserType.STUDENT)
                .course(course)
                .build();

        User teacher = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.TEACHER)
                .build();

        LocalDateTime now = LocalDateTime.now();
        Assignment assignment = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Due Now Assignment")
                .type(AssignmentType.HOMEWORK)
                .dueDate(now.plusSeconds(1))
                .assignedDate(now.minusDays(1))
                .course(course)
                .createdBy(teacher)
                .build();

        when(userService.getById(studentId)).thenReturn(student);
        when(gradeRepository.findByStudent(student)).thenReturn(Collections.emptyList());
        when(assignmentRepository.findByCourse(course)).thenReturn(List.of(assignment));
        when(userService.getStudentsByCourse(course)).thenReturn(List.of(student));

        StudentHomeViewDto result = studentHomeService.buildHomeView(studentId);

        assertNotNull(result);
        assertNotNull(result.getUpcomingAssignments());
        assertEquals(1, result.getUpcomingAssignments().size());
        verify(gradeRepository, times(2)).findByStudent(student);
    }

    @Test
    void buildHomeView_whenLeaderboardHasStudentsWithDifferentAverages_thenShouldSortCorrectly() {
        UUID studentId = UUID.randomUUID();
        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("Mathematics")
                .build();

        User student1 = User.builder()
                .id(studentId)
                .firstName("Alice")
                .lastName("Smith")
                .userType(UserType.STUDENT)
                .course(course)
                .studentClass("10A")
                .build();

        User student2 = User.builder()
                .id(UUID.randomUUID())
                .firstName("Bob")
                .lastName("Jones")
                .userType(UserType.STUDENT)
                .course(course)
                .studentClass("10A")
                .build();

        User teacher = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.TEACHER)
                .build();

        Grade grade1 = Grade.builder()
                .id(UUID.randomUUID())
                .student(student1)
                .subjectType(SubjectType.MATH)
                .gradeLetter(GradeLetter.EXCELLENT)
                .dateGraded(LocalDateTime.now())
                .gradedBy(teacher)
                .build();

        Grade grade2 = Grade.builder()
                .id(UUID.randomUUID())
                .student(student2)
                .subjectType(SubjectType.MATH)
                .gradeLetter(GradeLetter.GOOD)
                .dateGraded(LocalDateTime.now())
                .gradedBy(teacher)
                .build();

        Assignment assignment = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Test Assignment")
                .type(AssignmentType.HOMEWORK)
                .dueDate(LocalDateTime.now().plusDays(5))
                .assignedDate(LocalDateTime.now())
                .course(course)
                .createdBy(teacher)
                .build();

        when(userService.getById(studentId)).thenReturn(student1);
        when(gradeRepository.findByStudent(student1)).thenReturn(List.of(grade1));
        when(gradeRepository.findByStudent(student2)).thenReturn(List.of(grade2));
        when(assignmentRepository.findByCourse(course)).thenReturn(List.of(assignment));
        when(userService.getStudentsByCourse(course)).thenReturn(List.of(student1, student2));

        StudentHomeViewDto result = studentHomeService.buildHomeView(studentId);

        assertNotNull(result);
        assertNotNull(result.getLeaderboard());
        assertEquals(2, result.getLeaderboard().size());
        assertTrue(result.getLeaderboard().get(0).isCurrentUser());
        assertEquals("6.00", result.getLeaderboard().get(0).getGradeDisplay());
        verify(gradeRepository, times(2)).findByStudent(student1);
        verify(gradeRepository, times(1)).findByStudent(student2);
    }

    @Test
    void buildHomeView_whenStudentHasAssignmentsWithPastDueDates_thenShouldNotCountAsPending() {
        UUID studentId = UUID.randomUUID();
        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("Mathematics")
                .build();

        User student = User.builder()
                .id(studentId)
                .firstName("Alice")
                .lastName("Smith")
                .userType(UserType.STUDENT)
                .course(course)
                .build();

        User teacher = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.TEACHER)
                .build();

        LocalDateTime now = LocalDateTime.now();
        Assignment pastAssignment = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Past Assignment")
                .type(AssignmentType.HOMEWORK)
                .dueDate(now.minusDays(1))
                .assignedDate(now.minusDays(5))
                .course(course)
                .createdBy(teacher)
                .build();

        Assignment futureAssignment = Assignment.builder()
                .id(UUID.randomUUID())
                .title("Future Assignment")
                .type(AssignmentType.HOMEWORK)
                .dueDate(now.plusDays(5))
                .assignedDate(now)
                .course(course)
                .createdBy(teacher)
                .build();

        when(userService.getById(studentId)).thenReturn(student);
        when(gradeRepository.findByStudent(student)).thenReturn(Collections.emptyList());
        when(assignmentRepository.findByCourse(course)).thenReturn(List.of(pastAssignment, futureAssignment));
        when(userService.getStudentsByCourse(course)).thenReturn(List.of(student));

        StudentHomeViewDto result = studentHomeService.buildHomeView(studentId);

        assertNotNull(result);
        assertNotNull(result.getQuickStats());
        assertEquals(1, result.getQuickStats().getPendingAssignments());
        assertNotNull(result.getUpcomingAssignments());
        assertEquals(1, result.getUpcomingAssignments().size());
        verify(gradeRepository, times(2)).findByStudent(student);
    }
}

