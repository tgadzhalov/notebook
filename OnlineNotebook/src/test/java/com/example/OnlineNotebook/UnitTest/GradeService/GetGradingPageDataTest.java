package com.example.OnlineNotebook.UnitTest.GradeService;

import com.example.OnlineNotebook.models.dtos.teacher.grade.GradingPageDto;
import com.example.OnlineNotebook.models.entities.Course;
import com.example.OnlineNotebook.models.entities.Grade;
import com.example.OnlineNotebook.models.entities.User;
import com.example.OnlineNotebook.models.enums.GradeLetter;
import com.example.OnlineNotebook.models.enums.GradeType;
import com.example.OnlineNotebook.models.enums.SubjectType;
import com.example.OnlineNotebook.models.enums.UserType;
import com.example.OnlineNotebook.repositories.CourseRepository;
import com.example.OnlineNotebook.repositories.GradeRepository;
import com.example.OnlineNotebook.repositories.UserRepository;
import com.example.OnlineNotebook.services.GradeService;
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
class GetGradingPageDataTest {

    @InjectMocks
    private GradeService gradeService;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GradeRepository gradeRepository;

    @Test
    void getGradingPageData_whenCourseIdIsNull_thenShouldReturnDtoWithEmptyStudentsAndNoSelectedCourse() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .userType(UserType.TEACHER)
                .build();

        Course course1 = Course.builder()
                .id(UUID.randomUUID())
                .name("Maths")
                .teacher(teacher)
                .build();

        Course course2 = Course.builder()
                .id(UUID.randomUUID())
                .name("Physics")
                .teacher(teacher)
                .build();

        List<Course> teacherCourses = List.of(course1, course2);
        when(courseRepository.findByTeacher(teacher)).thenReturn(teacherCourses);

        GradingPageDto result = gradeService.getGradingPageData(teacher, null, null, null);

        assertNotNull(result);
        assertEquals(teacher, result.getUser());
        assertEquals(teacherCourses, result.getCourses());
        assertNull(result.getSelectedCourse());
        assertNull(result.getSelectedCourseId());
        assertTrue(result.getSubjects().isEmpty());
        assertTrue(result.getStudents().isEmpty());
        assertTrue(result.getStudentGrades().isEmpty());
        assertNull(result.getSelectedSubject());
        assertNull(result.getSelectedAssignmentId());
        assertEquals(GradeType.values().length, result.getAssignments().size());
        verify(courseRepository, times(1)).findByTeacher(teacher);
        verify(userRepository, never()).findByCourse(any());
        verify(gradeRepository, never()).findByStudent(any());
    }

    @Test
    void getGradingPageData_whenCourseIdIsNotNullAndSelectedCourseIsNull_thenShouldReturnDtoWithEmptyStudentsAndNoSelectedCourse() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .userType(UserType.TEACHER)
                .build();

        UUID nonExistentCourseId = UUID.randomUUID();
        List<Course> teacherCourses = Collections.emptyList();
        when(courseRepository.findByTeacher(teacher)).thenReturn(teacherCourses);

        GradingPageDto result = gradeService.getGradingPageData(teacher, nonExistentCourseId, null, null);

        assertNotNull(result);
        assertEquals(nonExistentCourseId, result.getSelectedCourseId());
        assertNull(result.getSelectedCourse());
        assertTrue(result.getStudents().isEmpty());
        assertTrue(result.getStudentGrades().isEmpty());
        assertNull(result.getSelectedSubject());
        assertNull(result.getSelectedAssignmentId());
        assertEquals(GradeType.values().length, result.getAssignments().size());
        verify(courseRepository, times(1)).findByTeacher(teacher);
        verify(userRepository, never()).findByCourse(any());
        verify(gradeRepository, never()).findByStudent(any());
    }

    @Test
    void getGradingPageData_whenCourseFoundButNoStudents_thenShouldReturnDtoWithEmptyStudents() {
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
                .subjects(List.of(SubjectType.MATH, SubjectType.ENGLISH))
                .build();

        List<Course> teacherCourses = List.of(course);
        when(courseRepository.findByTeacher(teacher)).thenReturn(teacherCourses);
        when(userRepository.findByCourse(course)).thenReturn(Collections.emptyList());

        GradingPageDto result = gradeService.getGradingPageData(teacher, course.getId(), null, null);

        assertNotNull(result);
        assertEquals(course.getId(), result.getSelectedCourseId());
        assertEquals(course, result.getSelectedCourse());
        assertEquals(List.of(SubjectType.MATH, SubjectType.ENGLISH), result.getSubjects());
        assertTrue(result.getStudents().isEmpty());
        assertTrue(result.getStudentGrades().isEmpty());
        assertNull(result.getSelectedSubject());
        assertNull(result.getSelectedAssignmentId());
        verify(courseRepository, times(1)).findByTeacher(teacher);
        verify(userRepository, times(1)).findByCourse(course);
        verify(gradeRepository, never()).findByStudent(any());
    }

    @Test
    void getGradingPageData_whenCourseFoundWithStudentsButNoSubjectType_thenShouldReturnDtoWithStudents() {
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
                .subjects(List.of(SubjectType.MATH))
                .build();

        User student1 = User.builder()
                .id(UUID.randomUUID())
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .userType(UserType.STUDENT)
                .course(course)
                .build();

        User student2 = User.builder()
                .id(UUID.randomUUID())
                .firstName("Bob")
                .lastName("Jones")
                .email("bob@example.com")
                .userType(UserType.STUDENT)
                .course(course)
                .build();

        List<Course> teacherCourses = List.of(course);
        List<User> courseUsers = List.of(student1, student2);

        when(courseRepository.findByTeacher(teacher)).thenReturn(teacherCourses);
        when(userRepository.findByCourse(course)).thenReturn(courseUsers);

        GradingPageDto result = gradeService.getGradingPageData(teacher, course.getId(), null, null);

        assertNotNull(result);
        assertEquals(course.getId(), result.getSelectedCourseId());
        assertEquals(course, result.getSelectedCourse());
        assertEquals(List.of(SubjectType.MATH), result.getSubjects());
        assertEquals(2, result.getStudents().size());
        assertTrue(result.getStudentGrades().isEmpty());
        assertNull(result.getSelectedSubject());
        assertNull(result.getSelectedAssignmentId());
        verify(courseRepository, times(1)).findByTeacher(teacher);
        verify(userRepository, times(1)).findByCourse(course);
        verify(gradeRepository, never()).findByStudent(any());
    }

    @Test
    void getGradingPageData_whenSubjectTypeProvidedButNoAssignmentId_thenShouldReturnDtoWithStudentsButNoGrades() {
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
                .subjects(List.of(SubjectType.MATH))
                .build();

        User student = User.builder()
                .id(UUID.randomUUID())
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .userType(UserType.STUDENT)
                .course(course)
                .build();

        List<Course> teacherCourses = List.of(course);
        List<User> courseUsers = List.of(student);

        when(courseRepository.findByTeacher(teacher)).thenReturn(teacherCourses);
        when(userRepository.findByCourse(course)).thenReturn(courseUsers);

        GradingPageDto result = gradeService.getGradingPageData(teacher, course.getId(), null, SubjectType.MATH.name());

        assertNotNull(result);
        assertEquals(course.getId(), result.getSelectedCourseId());
        assertEquals(course, result.getSelectedCourse());
        assertEquals(1, result.getStudents().size());
        assertTrue(result.getStudentGrades().isEmpty());
        assertEquals(SubjectType.MATH.name(), result.getSelectedSubject());
        assertNull(result.getSelectedAssignmentId());
        verify(courseRepository, times(1)).findByTeacher(teacher);
        verify(userRepository, times(1)).findByCourse(course);
        verify(gradeRepository, never()).findByStudent(any());
    }

    @Test
    void getGradingPageData_whenSubjectTypeAndAssignmentIdProvided_thenShouldReturnDtoWithStudentGrades() {
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
                .subjects(List.of(SubjectType.MATH))
                .build();

        User student1 = User.builder()
                .id(UUID.randomUUID())
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .userType(UserType.STUDENT)
                .course(course)
                .build();

        User student2 = User.builder()
                .id(UUID.randomUUID())
                .firstName("Bob")
                .lastName("Jones")
                .email("bob@example.com")
                .userType(UserType.STUDENT)
                .course(course)
                .build();

        Grade grade1 = Grade.builder()
                .id(UUID.randomUUID())
                .student(student1)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now())
                .build();

        List<Course> teacherCourses = List.of(course);
        List<User> courseUsers = List.of(student1, student2);
        List<Grade> student1Grades = List.of(grade1);
        List<Grade> student2Grades = Collections.emptyList();

        when(courseRepository.findByTeacher(teacher)).thenReturn(teacherCourses);
        when(userRepository.findByCourse(course)).thenReturn(courseUsers);
        when(gradeRepository.findByStudent(student1)).thenReturn(student1Grades);
        when(gradeRepository.findByStudent(student2)).thenReturn(student2Grades);

        UUID assignmentId = UUID.nameUUIDFromBytes(GradeType.TEST.name().getBytes());
        GradingPageDto result = gradeService.getGradingPageData(teacher, course.getId(), assignmentId, SubjectType.MATH.name());

        assertNotNull(result);
        assertEquals(course.getId(), result.getSelectedCourseId());
        assertEquals(course, result.getSelectedCourse());
        assertEquals(2, result.getStudents().size());
        assertEquals(1, result.getStudentGrades().size());
        assertTrue(result.getStudentGrades().containsKey(student1.getId()));
        assertEquals(grade1, result.getStudentGrades().get(student1.getId()));
        assertEquals(SubjectType.MATH.name(), result.getSelectedSubject());
        assertEquals(assignmentId, result.getSelectedAssignmentId());
        verify(courseRepository, times(1)).findByTeacher(teacher);
        verify(userRepository, times(1)).findByCourse(course);
        verify(gradeRepository, times(2)).findByStudent(any(User.class));
    }

    @Test
    void getGradingPageData_whenInvalidSubjectTypeProvided_thenShouldHandle() {
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
                .subjects(List.of(SubjectType.MATH))
                .build();

        User student = User.builder()
                .id(UUID.randomUUID())
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .userType(UserType.STUDENT)
                .course(course)
                .build();

        List<Course> teacherCourses = List.of(course);
        List<User> courseUsers = List.of(student);

        when(courseRepository.findByTeacher(teacher)).thenReturn(teacherCourses);
        when(userRepository.findByCourse(course)).thenReturn(courseUsers);

        GradingPageDto result = gradeService.getGradingPageData(teacher, course.getId(), null, "INVALID_SUBJECT");

        assertNotNull(result);
        assertEquals(course.getId(), result.getSelectedCourseId());
        assertEquals(course, result.getSelectedCourse());
        assertEquals(1, result.getStudents().size());
        assertTrue(result.getStudentGrades().isEmpty());
        assertEquals("INVALID_SUBJECT", result.getSelectedSubject());
        assertNull(result.getSelectedAssignmentId());
        verify(courseRepository, times(1)).findByTeacher(teacher);
        verify(userRepository, times(1)).findByCourse(course);
        verify(gradeRepository, never()).findByStudent(any());
    }

    @Test
    void getGradingPageData_whenCourseHasNullSubjects_thenShouldReturnEmptySubjectsList() {
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
                .subjects(null)
                .build();

        List<Course> teacherCourses = List.of(course);
        when(courseRepository.findByTeacher(teacher)).thenReturn(teacherCourses);
        when(userRepository.findByCourse(course)).thenReturn(Collections.emptyList());

        GradingPageDto result = gradeService.getGradingPageData(teacher, course.getId(), null, null);

        assertNotNull(result);
        assertEquals(course.getId(), result.getSelectedCourseId());
        assertEquals(course, result.getSelectedCourse());
        assertTrue(result.getSubjects().isEmpty());
        verify(courseRepository, times(1)).findByTeacher(teacher);
        verify(userRepository, times(1)).findByCourse(course);
    }

    @Test
    void getGradingPageData_whenAssignmentIdDoesNotMatchGradeType_thenShouldReturnDtoWithNullSelectedGradeType() {
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
                .subjects(List.of(SubjectType.MATH))
                .build();

        User student = User.builder()
                .id(UUID.randomUUID())
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .userType(UserType.STUDENT)
                .course(course)
                .build();

        List<Course> teacherCourses = List.of(course);
        List<User> courseUsers = List.of(student);

        when(courseRepository.findByTeacher(teacher)).thenReturn(teacherCourses);
        when(userRepository.findByCourse(course)).thenReturn(courseUsers);

        UUID invalidAssignmentId = UUID.randomUUID();
        GradingPageDto result = gradeService.getGradingPageData(teacher, course.getId(), invalidAssignmentId, SubjectType.MATH.name());

        assertNotNull(result);
        assertEquals(course.getId(), result.getSelectedCourseId());
        assertEquals(course, result.getSelectedCourse());
        assertTrue(result.getStudentGrades().isEmpty());
        assertEquals(SubjectType.MATH.name(), result.getSelectedSubject());
        assertEquals(invalidAssignmentId, result.getSelectedAssignmentId());
        verify(courseRepository, times(1)).findByTeacher(teacher);
        verify(userRepository, times(1)).findByCourse(course);
        verify(gradeRepository, never()).findByStudent(any());
    }

    @Test
    void getGradingPageData_whenNoGradeMatchesFilter_thenShouldReturnDtoWithEmptyStudentGrades() {
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
                .subjects(List.of(SubjectType.MATH))
                .build();

        User student = User.builder()
                .id(UUID.randomUUID())
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .userType(UserType.STUDENT)
                .course(course)
                .build();

        Grade grade = Grade.builder()
                .id(UUID.randomUUID())
                .student(student)
                .subjectType(SubjectType.ENGLISH)
                .gradeType(GradeType.PROJECT)
                .gradeLetter(GradeLetter.GOOD)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now())
                .build();

        List<Course> teacherCourses = List.of(course);
        List<User> courseUsers = List.of(student);
        List<Grade> studentGrades = List.of(grade);

        when(courseRepository.findByTeacher(teacher)).thenReturn(teacherCourses);
        when(userRepository.findByCourse(course)).thenReturn(courseUsers);
        when(gradeRepository.findByStudent(student)).thenReturn(studentGrades);

        UUID assignmentId = UUID.nameUUIDFromBytes(GradeType.TEST.name().getBytes());
        GradingPageDto result = gradeService.getGradingPageData(teacher, course.getId(), assignmentId, SubjectType.MATH.name());

        assertNotNull(result);
        assertTrue(result.getStudentGrades().isEmpty());
        verify(gradeRepository, times(1)).findByStudent(student);
    }
}
