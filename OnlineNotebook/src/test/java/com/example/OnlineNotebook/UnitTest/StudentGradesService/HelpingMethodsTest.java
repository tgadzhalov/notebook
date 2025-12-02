package com.example.OnlineNotebook.UnitTest.StudentGradesService;

import com.example.OnlineNotebook.models.entities.Grade;
import com.example.OnlineNotebook.models.entities.User;
import com.example.OnlineNotebook.models.enums.GradeLetter;
import com.example.OnlineNotebook.models.enums.GradeType;
import com.example.OnlineNotebook.models.enums.SubjectType;
import com.example.OnlineNotebook.repositories.GradeRepository;
import com.example.OnlineNotebook.services.StudentGradesService;
import com.example.OnlineNotebook.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HelpingMethodsTest {

    @InjectMocks
    private StudentGradesService studentGradesService;

    @Mock
    private UserService userService;

    @Mock
    private GradeRepository gradeRepository;

    @Test
    void resolveGradeValue_whenGradeLetterIsNull_thenReturnZero() throws Exception {
        Method method = StudentGradesService.class.getDeclaredMethod("resolveGradeValue", GradeLetter.class);
        method.setAccessible(true);
        Integer result = (Integer) method.invoke(studentGradesService, (GradeLetter) null);
        assertEquals(0, result);
    }

    @Test
    void resolveGradeValue_whenGradeLetterIsProvided_thenReturnCorrectValue() throws Exception {
        Method method = StudentGradesService.class.getDeclaredMethod("resolveGradeValue", GradeLetter.class);
        method.setAccessible(true);
        assertEquals(2, method.invoke(studentGradesService, GradeLetter.BAD));
        assertEquals(3, method.invoke(studentGradesService, GradeLetter.AVERAGE));
        assertEquals(4, method.invoke(studentGradesService, GradeLetter.GOOD));
        assertEquals(5, method.invoke(studentGradesService, GradeLetter.VERY_GOOD));
        assertEquals(6, method.invoke(studentGradesService, GradeLetter.EXCELLENT));
    }

    @Test
    void formatAverage_whenValueIsProvided_thenReturnFormattedString() throws Exception {
        Method method = StudentGradesService.class.getDeclaredMethod("formatAverage", double.class);
        method.setAccessible(true);
        assertEquals("4.50", method.invoke(studentGradesService, 4.5));
        assertEquals("3.25", method.invoke(studentGradesService, 3.25));
        assertEquals("5.00", method.invoke(studentGradesService, 5.0));
        assertEquals("2.33", method.invoke(studentGradesService, 2.333333));
    }

    @Test
    void buildInitials_whenNamesAreProvided_thenReturnInitials() throws Exception {
        Method method = StudentGradesService.class.getDeclaredMethod("buildInitials", String.class, String.class);
        method.setAccessible(true);
        assertEquals("JS", method.invoke(studentGradesService, "John", "Smith"));
        assertEquals("AB", method.invoke(studentGradesService, "Alice", "Brown"));
        assertEquals("--", method.invoke(studentGradesService, null, null));
        assertEquals("--", method.invoke(studentGradesService, "", ""));
        assertEquals("J", method.invoke(studentGradesService, "John", null));
        assertEquals("S", method.invoke(studentGradesService, null, "Smith"));
        assertEquals("--", method.invoke(studentGradesService, "", null));
        assertEquals("--", method.invoke(studentGradesService, null, ""));
    }

    @Test
    void buildTeacherName_whenTeacherIsNull_thenReturnDash() throws Exception {
        Method method = StudentGradesService.class.getDeclaredMethod("buildTeacherName", User.class);
        method.setAccessible(true);
        assertEquals("--", method.invoke(studentGradesService, (User) null));
    }

    @Test
    void buildTeacherName_whenTeacherHasName_thenReturnFullName() throws Exception {
        Method method = StudentGradesService.class.getDeclaredMethod("buildTeacherName", User.class);
        method.setAccessible(true);
        
        User teacher = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();
        assertEquals("John Doe", method.invoke(studentGradesService, teacher));
        
        User teacherWithNull = User.builder()
                .firstName(null)
                .lastName("Doe")
                .email("john@example.com")
                .build();
        assertEquals("Doe", method.invoke(studentGradesService, teacherWithNull));
        
        User teacherWithEmpty = User.builder()
                .firstName("")
                .lastName("")
                .email("john@example.com")
                .build();
        assertEquals("john@example.com", method.invoke(studentGradesService, teacherWithEmpty));
    }

    @Test
    void buildSubjectAverage_whenGradesProvided_thenReturnFormattedAverage() throws Exception {
        Method method = StudentGradesService.class.getDeclaredMethod("buildSubjectAverage", List.class);
        method.setAccessible(true);
        
        User student = User.builder().id(UUID.randomUUID()).build();
        User teacher = User.builder().id(UUID.randomUUID()).build();
        
        List<Grade> grades = List.of(
                Grade.builder()
                        .student(student)
                        .subjectType(SubjectType.MATH)
                        .gradeType(GradeType.TEST)
                        .gradeLetter(GradeLetter.GOOD)
                        .gradedBy(teacher)
                        .dateGraded(LocalDateTime.now())
                        .build(),
                Grade.builder()
                        .student(student)
                        .subjectType(SubjectType.MATH)
                        .gradeType(GradeType.PROJECT)
                        .gradeLetter(GradeLetter.EXCELLENT)
                        .gradedBy(teacher)
                        .dateGraded(LocalDateTime.now())
                        .build()
        );
        
        String result = (String) method.invoke(studentGradesService, grades);
        assertNotNull(result);
        assertTrue(result.contains("5.00"));
        assertTrue(result.contains("%"));
    }

    @Test
    void buildSubjectAverage_whenNoValidGrades_thenReturnDash() throws Exception {
        Method method = StudentGradesService.class.getDeclaredMethod("buildSubjectAverage", List.class);
        method.setAccessible(true);
        
        List<Grade> emptyGrades = List.of();
        assertEquals("--", method.invoke(studentGradesService, emptyGrades));
        
        User student = User.builder().id(UUID.randomUUID()).build();
        List<Grade> nullGradeLetters = List.of(
                Grade.builder()
                        .student(student)
                        .subjectType(SubjectType.MATH)
                        .gradeType(GradeType.TEST)
                        .gradeLetter(null)
                        .dateGraded(LocalDateTime.now())
                        .build()
        );
        assertEquals("--", method.invoke(studentGradesService, nullGradeLetters));
    }
}

