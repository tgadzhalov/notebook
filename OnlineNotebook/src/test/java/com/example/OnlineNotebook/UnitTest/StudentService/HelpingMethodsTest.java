package com.example.OnlineNotebook.UnitTest.StudentService;

import com.example.OnlineNotebook.client.service.AttendanceClientService;
import com.example.OnlineNotebook.models.entities.Course;
import com.example.OnlineNotebook.models.entities.User;
import com.example.OnlineNotebook.models.enums.AssignmentType;
import com.example.OnlineNotebook.models.enums.GradeLetter;
import com.example.OnlineNotebook.repositories.AssignmentRepository;
import com.example.OnlineNotebook.repositories.GradeRepository;
import com.example.OnlineNotebook.services.StudentService;
import com.example.OnlineNotebook.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HelpingMethodsTest {

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
    void resolveGradeValue_whenGradeLetterIsNull_thenReturnZero() throws Exception {
        Method method = StudentService.class.getDeclaredMethod("resolveGradeValue", GradeLetter.class);
        method.setAccessible(true);
        Integer result = (Integer) method.invoke(studentService, (GradeLetter) null);
        assertEquals(0, result);
    }

    @Test
    void resolveGradeValue_whenGradeLetterIsProvided_thenReturnCorrectValue() throws Exception {
        Method method = StudentService.class.getDeclaredMethod("resolveGradeValue", GradeLetter.class);
        method.setAccessible(true);
        assertEquals(2, method.invoke(studentService, GradeLetter.BAD));
        assertEquals(3, method.invoke(studentService, GradeLetter.AVERAGE));
        assertEquals(4, method.invoke(studentService, GradeLetter.GOOD));
        assertEquals(5, method.invoke(studentService, GradeLetter.VERY_GOOD));
        assertEquals(6, method.invoke(studentService, GradeLetter.EXCELLENT));
    }

    @Test
    void formatAverage_whenValueIsProvided_thenReturnFormattedString() throws Exception {
        Method method = StudentService.class.getDeclaredMethod("formatAverage", double.class);
        method.setAccessible(true);
        assertEquals("4.50", method.invoke(studentService, 4.5));
        assertEquals("3.25", method.invoke(studentService, 3.25));
        assertEquals("5.00", method.invoke(studentService, 5.0));
    }

    @Test
    void buildInitials_whenNamesAreProvided_thenReturnInitials() throws Exception {
        Method method = StudentService.class.getDeclaredMethod("buildInitials", String.class, String.class);
        method.setAccessible(true);
        assertEquals("JS", method.invoke(studentService, "John", "Smith"));
        assertEquals("AB", method.invoke(studentService, "Alice", "Brown"));
        assertEquals("--", method.invoke(studentService, null, null));
        assertEquals("--", method.invoke(studentService, "", ""));
        assertEquals("J", method.invoke(studentService, "John", null));
        assertEquals("S", method.invoke(studentService, null, "Smith"));
    }

    @Test
    void buildFullName_whenUserIsProvided_thenReturnFullName() throws Exception {
        Method method = StudentService.class.getDeclaredMethod("buildFullName", User.class);
        method.setAccessible(true);
        
        User user = User.builder()
                .firstName("John")
                .lastName("Smith")
                .build();
        assertEquals("John Smith", method.invoke(studentService, user));
        
        User userWithNull = User.builder()
                .firstName(null)
                .lastName("Smith")
                .build();
        assertEquals("Smith", method.invoke(studentService, userWithNull));
    }

    @Test
    void resolvePriorityLabel_whenDueDateIsWithin2Days_thenReturnHigh() throws Exception {
        Method method = StudentService.class.getDeclaredMethod("resolvePriorityLabel", LocalDateTime.class);
        method.setAccessible(true);
        LocalDateTime now = LocalDateTime.now();
        assertEquals("High", method.invoke(studentService, now.plusDays(1)));
        assertEquals("High", method.invoke(studentService, now.plusDays(2)));
    }

    @Test
    void resolvePriorityLabel_whenDueDateIsWithin5Days_thenReturnMedium() throws Exception {
        Method method = StudentService.class.getDeclaredMethod("resolvePriorityLabel", LocalDateTime.class);
        method.setAccessible(true);
        LocalDateTime now = LocalDateTime.now();
        assertEquals("Medium", method.invoke(studentService, now.plusDays(3)));
        assertEquals("Medium", method.invoke(studentService, now.plusDays(5)));
    }

    @Test
    void resolvePriorityLabel_whenDueDateIsMoreThan5Days_thenReturnLow() throws Exception {
        Method method = StudentService.class.getDeclaredMethod("resolvePriorityLabel", LocalDateTime.class);
        method.setAccessible(true);
        LocalDateTime now = LocalDateTime.now();
        assertEquals("Low", method.invoke(studentService, now.plusDays(6)));
        assertEquals("Low", method.invoke(studentService, now.plusDays(10)));
    }

    @Test
    void resolvePriorityLevel_whenDueDateIsWithin2Days_thenReturnHigh() throws Exception {
        Method method = StudentService.class.getDeclaredMethod("resolvePriorityLevel", LocalDateTime.class);
        method.setAccessible(true);
        LocalDateTime now = LocalDateTime.now();
        assertEquals("high", method.invoke(studentService, now.plusDays(1)));
        assertEquals("high", method.invoke(studentService, now.plusDays(2)));
    }

    @Test
    void resolvePriorityLevel_whenDueDateIsWithin5Days_thenReturnMedium() throws Exception {
        Method method = StudentService.class.getDeclaredMethod("resolvePriorityLevel", LocalDateTime.class);
        method.setAccessible(true);
        LocalDateTime now = LocalDateTime.now();
        assertEquals("medium", method.invoke(studentService, now.plusDays(3)));
        assertEquals("medium", method.invoke(studentService, now.plusDays(5)));
    }

    @Test
    void resolvePriorityLevel_whenDueDateIsMoreThan5Days_thenReturnLow() throws Exception {
        Method method = StudentService.class.getDeclaredMethod("resolvePriorityLevel", LocalDateTime.class);
        method.setAccessible(true);
        LocalDateTime now = LocalDateTime.now();
        assertEquals("low", method.invoke(studentService, now.plusDays(6)));
        assertEquals("low", method.invoke(studentService, now.plusDays(10)));
    }

    @Test
    void resolveAssignmentSubject_whenTypeIsNotNull_thenReturnTypeDisplay() throws Exception {
        Method method = StudentService.class.getDeclaredMethod("resolveAssignmentSubject", AssignmentType.class, Course.class);
        method.setAccessible(true);
        Course course = Course.builder().name("Math").build();
        assertEquals("Homework", method.invoke(studentService, AssignmentType.HOMEWORK, course));
        assertEquals("Test preparation", method.invoke(studentService, AssignmentType.TEST_PREPARATION, course));
    }

    @Test
    void resolveAssignmentSubject_whenTypeIsNull_thenReturnCourseName() throws Exception {
        Method method = StudentService.class.getDeclaredMethod("resolveAssignmentSubject", AssignmentType.class, Course.class);
        method.setAccessible(true);
        Course course = Course.builder().name("Mathematics").build();
        assertEquals("Mathematics", method.invoke(studentService, null, course));
    }

    @Test
    void resolveAssignmentSubject_whenTypeAndCourseAreNull_thenReturnCourse() throws Exception {
        Method method = StudentService.class.getDeclaredMethod("resolveAssignmentSubject", AssignmentType.class, Course.class);
        method.setAccessible(true);
        assertEquals("Course", method.invoke(studentService, null, null));
    }

    @Test
    void buildAttendanceDisplay_whenTotalAssignmentsIsZero_thenReturnDash() throws Exception {
        Method method = StudentService.class.getDeclaredMethod("buildAttendanceDisplay", int.class, int.class);
        method.setAccessible(true);
        assertEquals("--", method.invoke(studentService, 0, 0));
    }

    @Test
    void buildAttendanceDisplay_whenGradedEntriesProvided_thenReturnPercentage() throws Exception {
        Method method = StudentService.class.getDeclaredMethod("buildAttendanceDisplay", int.class, int.class);
        method.setAccessible(true);
        assertEquals("100%", method.invoke(studentService, 10, 10));
        assertEquals("50%", method.invoke(studentService, 10, 5));
        assertEquals("0%", method.invoke(studentService, 10, 0));
    }

    @Test
    void buildAbsencesDisplay_whenTotalAssignmentsIsZero_thenReturnDash() throws Exception {
        Method method = StudentService.class.getDeclaredMethod("buildAbsencesDisplay", int.class, int.class);
        method.setAccessible(true);
        assertEquals("--", method.invoke(studentService, 0, 0));
    }

    @Test
    void buildAbsencesDisplay_whenGradedEntriesProvided_thenReturnAbsences() throws Exception {
        Method method = StudentService.class.getDeclaredMethod("buildAbsencesDisplay", int.class, int.class);
        method.setAccessible(true);
        assertEquals("0", method.invoke(studentService, 10, 10));
        assertEquals("5", method.invoke(studentService, 10, 5));
        assertEquals("10", method.invoke(studentService, 10, 0));
    }
}

