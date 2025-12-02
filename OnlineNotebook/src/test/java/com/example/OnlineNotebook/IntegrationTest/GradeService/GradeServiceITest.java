package com.example.OnlineNotebook.IntegrationTest.GradeService;

import com.example.OnlineNotebook.exceptions.ResourceNotFoundException;
import com.example.OnlineNotebook.models.dtos.teacher.grade.UpdateGradeFeedbackDto;
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
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class GradeServiceITest {

    @Autowired
    private GradeService gradeService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    private User teacher;
    private User otherTeacher;
    private Course course;
    private Course otherCourse;
    private User student;

    @BeforeEach
    void setUp() {
        teacher = User.builder()
                .firstName("Ivan")
                .lastName("Blagoev")
                .email("ivan@test.com")
                .password(passwordEncoder.encode("password"))
                .userType(UserType.TEACHER)
                .build();
        teacher = userRepository.save(teacher);

        otherTeacher = User.builder()
                .firstName("Krum")
                .lastName("Krumov")
                .email("krum@test.com")
                .password(passwordEncoder.encode("password"))
                .userType(UserType.TEACHER)
                .build();
        otherTeacher = userRepository.save(otherTeacher);

        course = Course.builder()
                .name("10A")
                .description("Test Course")
                .schoolYear("2024-2025")
                .teacher(teacher)
                .subjects(List.of(SubjectType.MATH, SubjectType.ENGLISH))
                .build();
        course = courseRepository.save(course);

        otherCourse = Course.builder()
                .name("11A")
                .description("Other Course")
                .schoolYear("2024-2025")
                .teacher(otherTeacher)
                .subjects(List.of(SubjectType.MATH))
                .build();
        otherCourse = courseRepository.save(otherCourse);

        student = User.builder()
                .firstName("Petar")
                .lastName("Petrov")
                .email("petar@test.com")
                .password(passwordEncoder.encode("password"))
                .userType(UserType.STUDENT)
                .course(course)
                .studentClass("10A")
                .build();
        student = userRepository.save(student);
    }

    @Test
    void getGradingPageData_whenNoCourseSelected_thenReturnEmptyData() {
        var result = gradeService.getGradingPageData(teacher, null, null, null);

        assertNotNull(result);
        assertNotNull(result.getCourses());
        assertTrue(result.getCourses().size() > 0);
        assertNull(result.getSelectedCourse());
        assertNull(result.getSelectedCourseId());
        assertTrue(result.getStudents().isEmpty());
        assertTrue(result.getStudentGrades().isEmpty());
    }

    @Test
    void getGradingPageData_whenCourseSelected_thenReturnCourseData() {
        var result = gradeService.getGradingPageData(teacher, course.getId(), null, null);

        assertNotNull(result);
        assertNotNull(result.getSelectedCourse());
        assertEquals(course.getId(), result.getSelectedCourse().getId());
        assertEquals(course.getId(), result.getSelectedCourseId());
        assertNotNull(result.getSubjects());
        assertEquals(2, result.getSubjects().size());
        assertTrue(result.getSubjects().contains(SubjectType.MATH));
        assertTrue(result.getSubjects().contains(SubjectType.ENGLISH));
        assertNotNull(result.getStudents());
        assertEquals(1, result.getStudents().size());
        assertEquals(student.getId(), result.getStudents().get(0).getId());
    }

    @Test
    void getGradingPageData_whenSubjectAndAssignmentSelected_thenReturnGrades() {
        Grade grade = Grade.builder()
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now())
                .build();
        gradeRepository.save(grade);
        entityManager.flush();

        UUID testAssignmentId = getGradeTypeUuid(GradeType.TEST);

        var result = gradeService.getGradingPageData(teacher, course.getId(), testAssignmentId, SubjectType.MATH.name());

        assertNotNull(result);
        assertNotNull(result.getStudentGrades());
        assertEquals(1, result.getStudentGrades().size());
        assertTrue(result.getStudentGrades().containsKey(student.getId()));
        Grade retrievedGrade = result.getStudentGrades().get(student.getId());
        assertEquals(GradeLetter.GOOD, retrievedGrade.getGradeLetter());
    }

    @Test
    void getGradingPageData_whenInvalidSubject_thenHandleGracefully() {
        UUID testAssignmentId = getGradeTypeUuid(GradeType.TEST);

        var result = gradeService.getGradingPageData(teacher, course.getId(), testAssignmentId, "INVALID_SUBJECT");

        assertNotNull(result);
        assertTrue(result.getStudentGrades().isEmpty());
    }

    @Test
    void getStudentGradesForTeacher_whenValidRequest_thenReturnGrades() {
        Grade grade1 = Grade.builder()
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now().minusDays(2))
                .build();
        gradeRepository.save(grade1);

        Grade grade2 = Grade.builder()
                .student(student)
                .subjectType(SubjectType.ENGLISH)
                .gradeType(GradeType.PROJECT)
                .gradeLetter(GradeLetter.EXCELLENT)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now().minusDays(1))
                .build();
        gradeRepository.save(grade2);
        entityManager.flush();
        entityManager.clear();

        List<?> result = gradeService.getStudentGradesForTeacher(teacher, student.getId(), null);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getStudentGradesForTeacher_whenStudentNotFound_thenThrowException() {
        UUID nonExistentId = UUID.randomUUID();

        assertThrows(ResourceNotFoundException.class, () -> {
            gradeService.getStudentGradesForTeacher(teacher, nonExistentId, null);
        });
    }

    @Test
    void getStudentGradesForTeacher_whenStudentNotInCourse_thenThrowException() {
        final User studentWithoutCourse = User.builder()
                .firstName("Georgi")
                .lastName("Georgiev")
                .email("georgi@test.com")
                .password(passwordEncoder.encode("password"))
                .userType(UserType.STUDENT)
                .course(null)
                .build();
        userRepository.save(studentWithoutCourse);
        entityManager.flush();

        assertThrows(IllegalArgumentException.class, () -> {
            gradeService.getStudentGradesForTeacher(teacher, studentWithoutCourse.getId(), null);
        });
    }

    @Test
    void getStudentGradesForTeacher_whenCourseIdProvided_thenFilterByCourse() {
        Grade grade = Grade.builder()
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now())
                .build();
        gradeRepository.save(grade);
        entityManager.flush();
        entityManager.clear();

        List<?> result = gradeService.getStudentGradesForTeacher(teacher, student.getId(), course.getId());

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getStudentGradesForTeacher_whenTeacherNotOwner_thenThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            gradeService.getStudentGradesForTeacher(otherTeacher, student.getId(), null);
        });
    }

    @Test
    void getStudentGradesForTeacher_whenCourseIdNotMatching_thenThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            gradeService.getStudentGradesForTeacher(teacher, student.getId(), otherCourse.getId());
        });
    }

    @Test
    void deleteGradeForTeacher_whenTeacherOwnsCourse_thenDeleteGrade() {
        Grade grade = Grade.builder()
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now())
                .build();
        grade = gradeRepository.save(grade);
        entityManager.flush();
        UUID gradeId = grade.getId();

        gradeService.deleteGradeForTeacher(teacher, gradeId);
        entityManager.flush();
        entityManager.clear();

        assertFalse(gradeRepository.findById(gradeId).isPresent());
    }

    @Test
    void deleteGradeForTeacher_whenTeacherWasGrader_thenDeleteGrade() {
        Grade grade = Grade.builder()
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now())
                .build();
        grade = gradeRepository.save(grade);
        entityManager.flush();
        UUID gradeId = grade.getId();

        gradeService.deleteGradeForTeacher(teacher, gradeId);
        entityManager.flush();
        entityManager.clear();

        assertFalse(gradeRepository.findById(gradeId).isPresent());
    }

    @Test
    void deleteGradeForTeacher_whenTeacherNotAuthorized_thenThrowException() {
        Grade grade = Grade.builder()
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now())
                .build();
        grade = gradeRepository.save(grade);
        entityManager.flush();
        UUID gradeId = grade.getId();

        assertThrows(IllegalArgumentException.class, () -> {
            gradeService.deleteGradeForTeacher(otherTeacher, gradeId);
        });
    }

    @Test
    void deleteGradeForTeacher_whenGradeNotFound_thenThrowException() {
        UUID nonExistentId = UUID.randomUUID();

        assertThrows(ResourceNotFoundException.class, () -> {
            gradeService.deleteGradeForTeacher(teacher, nonExistentId);
        });
    }

    @Test
    void updateGradeFeedback_whenTeacherOwnsCourse_thenUpdateFeedback() {
        Grade grade = Grade.builder()
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now())
                .build();
        grade = gradeRepository.save(grade);
        entityManager.flush();
        UUID gradeId = grade.getId();

        UpdateGradeFeedbackDto updateDto = UpdateGradeFeedbackDto.builder()
                .feedback("Excellent work!")
                .build();

        gradeService.updateGradeFeedback(teacher, gradeId, updateDto);
        entityManager.flush();
        entityManager.clear();

        Grade updatedGrade = gradeRepository.findById(gradeId).orElse(null);
        assertNotNull(updatedGrade);
        assertEquals("Excellent work!", updatedGrade.getFeedback());
    }

    @Test
    void updateGradeFeedback_whenTeacherWasGrader_thenUpdateFeedback() {
        Grade grade = Grade.builder()
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now())
                .build();
        grade = gradeRepository.save(grade);
        entityManager.flush();
        UUID gradeId = grade.getId();

        UpdateGradeFeedbackDto updateDto = UpdateGradeFeedbackDto.builder()
                .feedback("Excellent progress")
                .build();

        gradeService.updateGradeFeedback(teacher, gradeId, updateDto);
        entityManager.flush();
        entityManager.clear();

        Grade updatedGrade = gradeRepository.findById(gradeId).orElse(null);
        assertNotNull(updatedGrade);
        assertEquals("Excellent progress", updatedGrade.getFeedback());
    }

    @Test
    void updateGradeFeedback_whenEmptyFeedback_thenSetToNull() {
        Grade grade = Grade.builder()
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now())
                .feedback("Original feedback")
                .build();
        grade = gradeRepository.save(grade);
        entityManager.flush();
        UUID gradeId = grade.getId();

        UpdateGradeFeedbackDto updateDto = UpdateGradeFeedbackDto.builder()
                .feedback("   ")
                .build();

        gradeService.updateGradeFeedback(teacher, gradeId, updateDto);
        entityManager.flush();
        entityManager.clear();

        Grade updatedGrade = gradeRepository.findById(gradeId).orElse(null);
        assertNotNull(updatedGrade);
        assertNull(updatedGrade.getFeedback());
    }

    @Test
    void updateGradeFeedback_whenNullDto_thenHandleGracefully() {
        Grade grade = Grade.builder()
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now())
                .feedback("Original")
                .build();
        grade = gradeRepository.save(grade);
        entityManager.flush();
        UUID gradeId = grade.getId();

        gradeService.updateGradeFeedback(teacher, gradeId, null);
        entityManager.flush();
        entityManager.clear();

        Grade updatedGrade = gradeRepository.findById(gradeId).orElse(null);
        assertNotNull(updatedGrade);
        assertNull(updatedGrade.getFeedback());
    }

    @Test
    void updateGradeFeedback_whenTeacherNotAuthorized_thenThrowException() {
        Grade grade = Grade.builder()
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now())
                .build();
        grade = gradeRepository.save(grade);
        entityManager.flush();
        UUID gradeId = grade.getId();

        UpdateGradeFeedbackDto updateDto = UpdateGradeFeedbackDto.builder()
                .feedback("Feedback")
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            gradeService.updateGradeFeedback(otherTeacher, gradeId, updateDto);
        });
    }

    @Test
    void updateGradeFeedback_whenGradeNotFound_thenThrowException() {
        UUID nonExistentId = UUID.randomUUID();
        UpdateGradeFeedbackDto updateDto = UpdateGradeFeedbackDto.builder()
                .feedback("Feedback")
                .build();

        assertThrows(ResourceNotFoundException.class, () -> {
            gradeService.updateGradeFeedback(teacher, nonExistentId, updateDto);
        });
    }

    private UUID getGradeTypeUuid(GradeType gradeType) {
        return UUID.nameUUIDFromBytes(gradeType.name().getBytes(StandardCharsets.UTF_8));
    }
}
