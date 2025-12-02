package com.example.OnlineNotebook.UnitTest.GradeService;

import com.example.OnlineNotebook.exceptions.ResourceNotFoundException;
import com.example.OnlineNotebook.models.dtos.teacher.student.TeacherStudentGradeDto;
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
import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetStudentGradesForTeacherTest {

    @InjectMocks
    private GradeService gradeService;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GradeRepository gradeRepository;

    @Test
    void getStudentGradesForTeacher_whenStudentNotFound_thenThrowResourceNotFoundException() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .email("random@email.com")
                .password("12312312")
                .userType(UserType.TEACHER)
                .studentClass(null)
                .firstName("Ivan")
                .lastName("Petrov")
                .createdAt(LocalDateTime.now())
                .build();

        UUID studentId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        when(userRepository.findById(studentId)).thenReturn(empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> gradeService.getStudentGradesForTeacher(teacher, studentId, courseId));

        assertEquals("Student not found", exception.getMessage());
        verify(userRepository, times(1)).findById(studentId);
        verify(courseRepository, never()).findById(any());
        verify(gradeRepository, never()).findByStudent(any());
    }

    @Test
    void getStudentGradesForTeacher_whenStudentCourseIsNull_thenThrowIllegalArgumentException() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .email("random@email.com")
                .password("12312312")
                .userType(UserType.TEACHER)
                .studentClass(null)
                .firstName("Ivan")
                .lastName("Petrov")
                .createdAt(LocalDateTime.now())
                .build();

        User student = User.builder()
                .id(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Petrov")
                .course(null)
                .email("random@email.com")
                .studentClass("11A")
                .password("12312312")
                .createdAt(LocalDateTime.now())
                .build();

        UUID studentId = UUID.randomUUID();


        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> gradeService.getStudentGradesForTeacher(teacher, studentId, null));

        assertEquals("Student is not assigned to a course", exception.getMessage());
        verify(userRepository, times(1)).findById(studentId);
        verify(courseRepository, never()).findById(any());
        verify(gradeRepository, never()).findByStudent(any());
    }

    @Test
    void getStudentGradesForTeacher_whenCourseIdIsNullAndCourseTeacherIsNull_thenThrowIllegalArgumentException() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .email("random@email.com")
                .password("12312312")
                .userType(UserType.TEACHER)
                .studentClass(null)
                .firstName("Ivan")
                .lastName("Petrov")
                .createdAt(LocalDateTime.now())
                .build();

        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("11A")
                .subjects(List.of(SubjectType.BULGARIAN))
                .description("Something")
                .teacher(null)
                .build();

        User student = User.builder()
                .id(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Petrov")
                .course(course)
                .email("random@email.com")
                .studentClass("11A")
                .password("12312312")
                .createdAt(LocalDateTime.now())
                .build();

        UUID studentId = student.getId();

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> gradeService.getStudentGradesForTeacher(teacher, studentId, null));

        assertEquals("You are not allowed to view grades for this student", exception.getMessage());
        verify(userRepository, times(1)).findById(studentId);
        verify(courseRepository, never()).findById(any());
        verify(gradeRepository, never()).findByStudent(any());
    }

    @Test
    void getStudentGradesForTeacher_whenCourseIdIsNullAndCourseTeacherIdDontMatch_thenThrowIllegalArgumentException() {
        UUID teacher1Id = UUID.randomUUID();
        UUID teacher2Id = UUID.randomUUID();

        User teacher1 = User.builder()
                .id(teacher1Id)
                .email("teacher1@email.com")
                .password("12312312")
                .userType(UserType.TEACHER)
                .studentClass(null)
                .firstName("Ivan")
                .lastName("Petrov")
                .createdAt(LocalDateTime.now())
                .build();

        User teacher2 = User.builder()
                .id(teacher2Id)
                .email("teacher2@email.com")
                .password("12312312")
                .userType(UserType.TEACHER)
                .studentClass(null)
                .firstName("Petar")
                .lastName("Ivanov")
                .createdAt(LocalDateTime.now())
                .build();

        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("11A")
                .subjects(List.of(SubjectType.BULGARIAN))
                .description("Something")
                .teacher(teacher2)
                .build();

        User student = User.builder()
                .id(UUID.randomUUID())
                .firstName("Student")
                .lastName("Name")
                .course(course)
                .email("student@email.com")
                .studentClass("11A")
                .password("12312312")
                .createdAt(LocalDateTime.now())
                .build();

        UUID studentId = student.getId();

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> gradeService.getStudentGradesForTeacher(teacher1, studentId, null));

        assertEquals("You are not allowed to view grades for this student", exception.getMessage());
        verify(userRepository, times(1)).findById(studentId);
        verify(courseRepository, never()).findById(any());
        verify(gradeRepository, never()).findByStudent(any());
    }

    @Test
    void getStudentGradesForTeacher_whenCourseIdIsNotNullAndCourseNotFound_thenThrowResourceNotFoundException() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .email("teacher@email.com")
                .password("12312312")
                .userType(UserType.TEACHER)
                .studentClass(null)
                .firstName("Ivan")
                .lastName("Petrov")
                .createdAt(LocalDateTime.now())
                .build();

        Course studentCourse = Course.builder()
                .id(UUID.randomUUID())
                .name("11A")
                .subjects(List.of(SubjectType.BULGARIAN))
                .teacher(teacher)
                .build();

        User student = User.builder()
                .id(UUID.randomUUID())
                .firstName("Student")
                .lastName("Name")
                .course(studentCourse)
                .email("student@email.com")
                .studentClass("11A")
                .password("12312312")
                .createdAt(LocalDateTime.now())
                .build();

        UUID studentId = student.getId();
        UUID nonExistentCourseId = UUID.randomUUID();

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(courseRepository.findById(nonExistentCourseId)).thenReturn(empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> gradeService.getStudentGradesForTeacher(teacher, studentId, nonExistentCourseId));

        assertEquals("Course not found", exception.getMessage());
        verify(userRepository, times(1)).findById(studentId);
        verify(courseRepository, times(1)).findById(nonExistentCourseId);
        verify(gradeRepository, never()).findByStudent(any());
    }

    @Test
    void getStudentGradesForTeacher_whenCourseIdIsNotNullAndCourseTeacherDontMatch_thenThrowIllegalArgumentException() {
        UUID teacher1Id = UUID.randomUUID();
        UUID teacher2Id = UUID.randomUUID();

        User teacher1 = User.builder()
                .id(teacher1Id)
                .email("teacher1@email.com")
                .password("12312312")
                .userType(UserType.TEACHER)
                .studentClass(null)
                .firstName("Ivan")
                .lastName("Petrov")
                .createdAt(LocalDateTime.now())
                .build();

        User teacher2 = User.builder()
                .id(teacher2Id)
                .email("teacher2@email.com")
                .password("12312312")
                .userType(UserType.TEACHER)
                .studentClass(null)
                .firstName("Petar")
                .lastName("Ivanov")
                .createdAt(LocalDateTime.now())
                .build();

        Course studentCourse = Course.builder()
                .id(UUID.randomUUID())
                .name("11A")
                .subjects(List.of(SubjectType.BULGARIAN))
                .teacher(teacher2)
                .build();

        Course selectedCourse = Course.builder()
                .id(UUID.randomUUID())
                .name("11B")
                .subjects(List.of(SubjectType.MATH))
                .teacher(teacher2)
                .build();

        User student = User.builder()
                .id(UUID.randomUUID())
                .firstName("Student")
                .lastName("Name")
                .course(studentCourse)
                .email("student@email.com")
                .studentClass("11A")
                .password("12312312")
                .createdAt(LocalDateTime.now())
                .build();

        UUID studentId = student.getId();
        UUID selectedCourseId = selectedCourse.getId();

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(courseRepository.findById(selectedCourseId)).thenReturn(Optional.of(selectedCourse));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> gradeService.getStudentGradesForTeacher(teacher1, studentId, selectedCourseId));

        assertEquals("You are not allowed to view grades for this course", exception.getMessage());
        verify(userRepository, times(1)).findById(studentId);
        verify(courseRepository, times(1)).findById(selectedCourseId);
        verify(gradeRepository, never()).findByStudent(any());
    }

    @Test
    void getStudentGradesForTeacher_whenCourseIdIsNotNullAndStudentNotPartOfCourse_thenThrowIllegalArgumentException() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .email("teacher@email.com")
                .password("12312312")
                .userType(UserType.TEACHER)
                .studentClass(null)
                .firstName("Ivan")
                .lastName("Petrov")
                .createdAt(LocalDateTime.now())
                .build();

        Course studentCourse = Course.builder()
                .id(UUID.randomUUID())
                .name("11A")
                .subjects(List.of(SubjectType.BULGARIAN))
                .teacher(teacher)
                .build();

        Course selectedCourse = Course.builder()
                .id(UUID.randomUUID())
                .name("11B")
                .subjects(List.of(SubjectType.MATH))
                .teacher(teacher)
                .build();

        User student = User.builder()
                .id(UUID.randomUUID())
                .firstName("Student")
                .lastName("Name")
                .course(studentCourse)
                .email("student@email.com")
                .studentClass("11A")
                .password("12312312")
                .createdAt(LocalDateTime.now())
                .build();

        UUID studentId = student.getId();
        UUID selectedCourseId = selectedCourse.getId();

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(courseRepository.findById(selectedCourseId)).thenReturn(Optional.of(selectedCourse));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> gradeService.getStudentGradesForTeacher(teacher, studentId, selectedCourseId));

        assertEquals("Student is not part of the selected course", exception.getMessage());
        verify(userRepository, times(1)).findById(studentId);
        verify(courseRepository, times(1)).findById(selectedCourseId);
        verify(gradeRepository, never()).findByStudent(any());
    }

    @Test
    void getStudentGradesForTeacher_whenValidRequest_thenReturnGradesList() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .email("teacher@email.com")
                .password("12312312")
                .userType(UserType.TEACHER)
                .studentClass(null)
                .firstName("Ivan")
                .lastName("Petrov")
                .createdAt(LocalDateTime.now())
                .build();

        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("11A")
                .subjects(List.of(SubjectType.MATH, SubjectType.ENGLISH))
                .teacher(teacher)
                .build();

        User student = User.builder()
                .id(UUID.randomUUID())
                .firstName("Student")
                .lastName("Name")
                .course(course)
                .email("student@email.com")
                .studentClass("11A")
                .password("12312312")
                .createdAt(LocalDateTime.now())
                .build();

        Grade grade1 = Grade.builder()
                .id(UUID.randomUUID())
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now().minusDays(5))
                .build();

        Grade grade2 = Grade.builder()
                .id(UUID.randomUUID())
                .student(student)
                .subjectType(SubjectType.ENGLISH)
                .gradeType(GradeType.PROJECT)
                .gradeLetter(GradeLetter.EXCELLENT)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now().minusDays(2))
                .build();

        Grade grade3 = Grade.builder()
                .id(UUID.randomUUID())
                .student(student)
                .subjectType(SubjectType.SCIENCE)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.AVERAGE)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now().minusDays(1))
                .build();

        UUID studentId = student.getId();

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(gradeRepository.findByStudent(student)).thenReturn(List.of(grade1, grade2, grade3));

        List<TeacherStudentGradeDto> result = gradeService.getStudentGradesForTeacher(teacher, studentId, null);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(dto -> dto.getSubjectCode().equals(SubjectType.MATH.name())));
        assertTrue(result.stream().anyMatch(dto -> dto.getSubjectCode().equals(SubjectType.ENGLISH.name())));
        assertFalse(result.stream().anyMatch(dto -> dto.getSubjectCode().equals(SubjectType.SCIENCE.name())));
        verify(userRepository, times(1)).findById(studentId);
        verify(gradeRepository, times(1)).findByStudent(student);
        verify(courseRepository, never()).findById(any());
    }

    @Test
    void getStudentGradesForTeacher_whenStudentCourseIdIsNull_thenThrowIllegalArgumentException() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .email("teacher@email.com")
                .password("12312312")
                .userType(UserType.TEACHER)
                .studentClass(null)
                .firstName("Ivan")
                .lastName("Petrov")
                .createdAt(LocalDateTime.now())
                .build();

        Course studentCourse = Course.builder()
                .id(null)
                .name("11A")
                .subjects(List.of(SubjectType.MATH))
                .teacher(teacher)
                .build();

        Course selectedCourse = Course.builder()
                .id(UUID.randomUUID())
                .name("11B")
                .subjects(List.of(SubjectType.MATH))
                .teacher(teacher)
                .build();

        User student = User.builder()
                .id(UUID.randomUUID())
                .firstName("Student")
                .lastName("Name")
                .course(studentCourse)
                .email("student@email.com")
                .studentClass("11A")
                .password("12312312")
                .createdAt(LocalDateTime.now())
                .build();

        UUID studentId = student.getId();
        UUID selectedCourseId = selectedCourse.getId();

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(courseRepository.findById(selectedCourseId)).thenReturn(Optional.of(selectedCourse));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> gradeService.getStudentGradesForTeacher(teacher, studentId, selectedCourseId));

        assertEquals("Student is not part of the selected course", exception.getMessage());
        verify(userRepository, times(1)).findById(studentId);
        verify(courseRepository, times(1)).findById(selectedCourseId);
        verify(gradeRepository, never()).findByStudent(any());
    }

    @Test
    void getStudentGradesForTeacher_whenCourseSubjectsIsNull_thenShouldReturnAllGrades() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .email("teacher@email.com")
                .password("12312312")
                .userType(UserType.TEACHER)
                .studentClass(null)
                .firstName("Ivan")
                .lastName("Petrov")
                .createdAt(LocalDateTime.now())
                .build();

        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("11A")
                .subjects(null)
                .teacher(teacher)
                .build();

        User student = User.builder()
                .id(UUID.randomUUID())
                .firstName("Student")
                .lastName("Name")
                .course(course)
                .email("student@email.com")
                .studentClass("11A")
                .password("12312312")
                .createdAt(LocalDateTime.now())
                .build();

        Grade grade = Grade.builder()
                .id(UUID.randomUUID())
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now())
                .build();

        UUID studentId = student.getId();

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(gradeRepository.findByStudent(student)).thenReturn(List.of(grade));

        List<TeacherStudentGradeDto> result = gradeService.getStudentGradesForTeacher(teacher, studentId, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository, times(1)).findById(studentId);
        verify(gradeRepository, times(1)).findByStudent(student);
    }

    @Test
    void getStudentGradesForTeacher_whenCourseSubjectsIsEmpty_thenShouldReturnAllGrades() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .email("teacher@email.com")
                .password("12312312")
                .userType(UserType.TEACHER)
                .studentClass(null)
                .firstName("Ivan")
                .lastName("Petrov")
                .createdAt(LocalDateTime.now())
                .build();

        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("11A")
                .subjects(Collections.emptyList())
                .teacher(teacher)
                .build();

        User student = User.builder()
                .id(UUID.randomUUID())
                .firstName("Student")
                .lastName("Name")
                .course(course)
                .email("student@email.com")
                .studentClass("11A")
                .password("12312312")
                .createdAt(LocalDateTime.now())
                .build();

        Grade grade = Grade.builder()
                .id(UUID.randomUUID())
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now())
                .build();

        UUID studentId = student.getId();

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(gradeRepository.findByStudent(student)).thenReturn(List.of(grade));

        List<TeacherStudentGradeDto> result = gradeService.getStudentGradesForTeacher(teacher, studentId, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository, times(1)).findById(studentId);
        verify(gradeRepository, times(1)).findByStudent(student);
    }

    @Test
    void getStudentGradesForTeacher_whenGradeHasNullGradeLetter_thenShouldHandleGracefully() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .email("teacher@email.com")
                .password("12312312")
                .userType(UserType.TEACHER)
                .studentClass(null)
                .firstName("Ivan")
                .lastName("Petrov")
                .createdAt(LocalDateTime.now())
                .build();

        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("11A")
                .subjects(List.of(SubjectType.MATH))
                .teacher(teacher)
                .build();

        User student = User.builder()
                .id(UUID.randomUUID())
                .firstName("Student")
                .lastName("Name")
                .course(course)
                .email("student@email.com")
                .studentClass("11A")
                .password("12312312")
                .createdAt(LocalDateTime.now())
                .build();

        Grade grade = Grade.builder()
                .id(UUID.randomUUID())
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(null)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now())
                .build();

        UUID studentId = student.getId();

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(gradeRepository.findByStudent(student)).thenReturn(List.of(grade));

        List<TeacherStudentGradeDto> result = gradeService.getStudentGradesForTeacher(teacher, studentId, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getGradeLetter());
        assertNull(result.get(0).getGradeValue());
        assertNull(result.get(0).getGradeDisplay());
        verify(userRepository, times(1)).findById(studentId);
        verify(gradeRepository, times(1)).findByStudent(student);
    }

    @Test
    void getStudentGradesForTeacher_whenGradeHasNullGradedBy_thenShouldHandleGracefully() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .email("teacher@email.com")
                .password("12312312")
                .userType(UserType.TEACHER)
                .studentClass(null)
                .firstName("Ivan")
                .lastName("Petrov")
                .createdAt(LocalDateTime.now())
                .build();

        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("11A")
                .subjects(List.of(SubjectType.MATH))
                .teacher(teacher)
                .build();

        User student = User.builder()
                .id(UUID.randomUUID())
                .firstName("Student")
                .lastName("Name")
                .course(course)
                .email("student@email.com")
                .studentClass("11A")
                .password("12312312")
                .createdAt(LocalDateTime.now())
                .build();

        Grade grade = Grade.builder()
                .id(UUID.randomUUID())
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .gradedBy(null)
                .dateGraded(LocalDateTime.now())
                .build();

        UUID studentId = student.getId();

        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(gradeRepository.findByStudent(student)).thenReturn(List.of(grade));

        List<TeacherStudentGradeDto> result = gradeService.getStudentGradesForTeacher(teacher, studentId, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get(0).getGradedBy());
        verify(userRepository, times(1)).findById(studentId);
        verify(gradeRepository, times(1)).findByStudent(student);
    }
}
