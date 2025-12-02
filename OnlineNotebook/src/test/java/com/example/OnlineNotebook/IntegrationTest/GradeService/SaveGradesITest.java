package com.example.OnlineNotebook.IntegrationTest.GradeService;

import com.example.OnlineNotebook.exceptions.ResourceNotFoundException;
import com.example.OnlineNotebook.models.dtos.teacher.grade.SaveGradesDto;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class SaveGradesITest {

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
    private Course course;
    private User student1;
    private User student2;
    private User student3;

    @BeforeEach
    void setUp() {
        // Create teacher
        teacher = User.builder()
                .firstName("John")
                .lastName("Teacher")
                .email("teacher@test.com")
                .password(passwordEncoder.encode("password"))
                .userType(UserType.TEACHER)
                .build();
        teacher = userRepository.save(teacher);

        // Create course with subjects
        course = Course.builder()
                .name("10A")
                .description("Test Course")
                .schoolYear("2024-2025")
                .teacher(teacher)
                .subjects(List.of(SubjectType.MATH, SubjectType.ENGLISH))
                .build();
        course = courseRepository.save(course);

        // Create students
        student1 = User.builder()
                .firstName("Alice")
                .lastName("Student")
                .email("alice@test.com")
                .password(passwordEncoder.encode("password"))
                .userType(UserType.STUDENT)
                .course(course)
                .studentClass("10A")
                .build();
        student1 = userRepository.save(student1);

        student2 = User.builder()
                .firstName("Bob")
                .lastName("Student")
                .email("bob@test.com")
                .password(passwordEncoder.encode("password"))
                .userType(UserType.STUDENT)
                .course(course)
                .studentClass("10A")
                .build();
        student2 = userRepository.save(student2);

        student3 = User.builder()
                .firstName("Charlie")
                .lastName("Student")
                .email("charlie@test.com")
                .password(passwordEncoder.encode("password"))
                .userType(UserType.STUDENT)
                .course(course)
                .studentClass("10A")
                .build();
        student3 = userRepository.save(student3);
    }

    @Test
    void saveGrades_whenValidData_thenSaveNewGrades() {
        // Given
        SaveGradesDto saveGradesDto = SaveGradesDto.builder()
                .courseId(course.getId())
                .assignmentId(UUID.randomUUID()) // This will be converted to GradeType
                .subjectType(SubjectType.MATH.name())
                .gradeDate(LocalDate.now())
                .studentGrades(Map.of(
                        student1.getId().toString(), "5",
                        student2.getId().toString(), "4",
                        student3.getId().toString(), "6"
                ))
                .build();

        // Get the actual assignmentId by finding the GradeType.TEST UUID
        UUID testAssignmentId = getGradeTypeUuid(GradeType.TEST);
        saveGradesDto.setAssignmentId(testAssignmentId);

        // When
        gradeService.saveGrades(saveGradesDto, teacher);
        entityManager.flush();
        entityManager.clear();

        // Then - reload students to get fresh entities
        User reloadedStudent1 = userRepository.findById(student1.getId()).orElse(null);
        User reloadedStudent2 = userRepository.findById(student2.getId()).orElse(null);
        User reloadedStudent3 = userRepository.findById(student3.getId()).orElse(null);
        
        List<Grade> savedGrades = gradeRepository.findAll();
        assertEquals(3, savedGrades.size());

        Grade grade1 = gradeRepository.findByStudent(reloadedStudent1).stream()
                .filter(g -> g.getSubjectType() == SubjectType.MATH && g.getGradeType() == GradeType.TEST)
                .findFirst()
                .orElse(null);
        assertNotNull(grade1);
        assertEquals(GradeLetter.VERY_GOOD, grade1.getGradeLetter());
        assertEquals(teacher.getId(), grade1.getGradedBy().getId());
        assertEquals(student1.getId(), grade1.getStudent().getId());

        Grade grade2 = gradeRepository.findByStudent(reloadedStudent2).stream()
                .filter(g -> g.getSubjectType() == SubjectType.MATH && g.getGradeType() == GradeType.TEST)
                .findFirst()
                .orElse(null);
        assertNotNull(grade2);
        assertEquals(GradeLetter.GOOD, grade2.getGradeLetter());

        Grade grade3 = gradeRepository.findByStudent(reloadedStudent3).stream()
                .filter(g -> g.getSubjectType() == SubjectType.MATH && g.getGradeType() == GradeType.TEST)
                .findFirst()
                .orElse(null);
        assertNotNull(grade3);
        assertEquals(GradeLetter.EXCELLENT, grade3.getGradeLetter());
    }

    @Test
    void saveGrades_whenGradeExists_thenUpdateGrade() {
        // Given - create existing grade
        Grade existingGrade = Grade.builder()
                .student(student1)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.AVERAGE)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now().minusDays(5))
                .build();
        gradeRepository.save(existingGrade);
        entityManager.flush();
        UUID existingGradeId = existingGrade.getId();

        UUID testAssignmentId = getGradeTypeUuid(GradeType.TEST);
        SaveGradesDto saveGradesDto = SaveGradesDto.builder()
                .courseId(course.getId())
                .assignmentId(testAssignmentId)
                .subjectType(SubjectType.MATH.name())
                .gradeDate(LocalDate.now())
                .studentGrades(Map.of(
                        student1.getId().toString(), "6" // Update from 3 to 6
                ))
                .build();

        // When
        gradeService.saveGrades(saveGradesDto, teacher);
        entityManager.flush();
        entityManager.clear();

        // Then
        List<Grade> allGrades = gradeRepository.findAll();
        assertEquals(1, allGrades.size());

        Grade updatedGrade = gradeRepository.findById(existingGradeId).orElse(null);
        assertNotNull(updatedGrade);
        assertEquals(GradeLetter.EXCELLENT, updatedGrade.getGradeLetter());
        assertEquals(teacher.getId(), updatedGrade.getGradedBy().getId());
        // Verify date was updated
        assertEquals(LocalDate.now(), updatedGrade.getDateGraded().toLocalDate());
    }

    @Test
    void saveGrades_whenEmptyGradeValue_thenDeleteGrade() {
        // Given - create existing grade
        Grade existingGrade = Grade.builder()
                .student(student1)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now())
                .build();
        gradeRepository.save(existingGrade);
        entityManager.flush();
        UUID existingGradeId = existingGrade.getId();

        UUID testAssignmentId = getGradeTypeUuid(GradeType.TEST);
        SaveGradesDto saveGradesDto = SaveGradesDto.builder()
                .courseId(course.getId())
                .assignmentId(testAssignmentId)
                .subjectType(SubjectType.MATH.name())
                .gradeDate(LocalDate.now())
                .studentGrades(Map.of(
                        student1.getId().toString(), "" // Empty value should delete grade
                ))
                .build();

        // When
        gradeService.saveGrades(saveGradesDto, teacher);
        entityManager.flush();
        entityManager.clear();

        // Then
        List<Grade> allGrades = gradeRepository.findAll();
        assertEquals(0, allGrades.size());
        
        assertFalse(gradeRepository.findById(existingGradeId).isPresent());
    }

    @Test
    void saveGrades_whenCourseNotFound_thenThrowResourceNotFoundException() {
        // Given
        UUID nonExistentCourseId = UUID.randomUUID();
        UUID testAssignmentId = getGradeTypeUuid(GradeType.TEST);
        SaveGradesDto saveGradesDto = SaveGradesDto.builder()
                .courseId(nonExistentCourseId)
                .assignmentId(testAssignmentId)
                .subjectType(SubjectType.MATH.name())
                .gradeDate(LocalDate.now())
                .studentGrades(Map.of(student1.getId().toString(), "5"))
                .build();

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            gradeService.saveGrades(saveGradesDto, teacher);
        });
    }

    @Test
    void saveGrades_whenCourseHasNoSubjects_thenThrowIllegalArgumentException() {
        // Given - create course without subjects (empty list)
        Course courseWithoutSubjects = Course.builder()
                .name("Empty Course")
                .description("Course without subjects")
                .schoolYear("2024-2025")
                .teacher(teacher)
                .subjects(List.of()) // Empty list
                .build();
        courseWithoutSubjects = courseRepository.save(courseWithoutSubjects);
        entityManager.flush();
        entityManager.clear();
        
        // Reload to ensure subjects is empty
        courseWithoutSubjects = courseRepository.findById(courseWithoutSubjects.getId()).orElse(null);
        assertNotNull(courseWithoutSubjects);
        assertTrue(courseWithoutSubjects.getSubjects() == null || courseWithoutSubjects.getSubjects().isEmpty());

        UUID testAssignmentId = getGradeTypeUuid(GradeType.TEST);
        SaveGradesDto saveGradesDto = SaveGradesDto.builder()
                .courseId(courseWithoutSubjects.getId())
                .assignmentId(testAssignmentId)
                .subjectType(SubjectType.MATH.name())
                .gradeDate(LocalDate.now())
                .studentGrades(Map.of(student1.getId().toString(), "5"))
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            gradeService.saveGrades(saveGradesDto, teacher);
        });
    }

    @Test
    void saveGrades_whenInvalidGradeType_thenThrowResourceNotFoundException() {
        // Given - use a UUID that definitely won't match any GradeType
        // Generate a UUID that's different from all GradeType UUIDs
        UUID invalidAssignmentId;
        do {
            invalidAssignmentId = UUID.randomUUID();
        } while (isValidGradeTypeUuid(invalidAssignmentId));
        
        SaveGradesDto saveGradesDto = SaveGradesDto.builder()
                .courseId(course.getId())
                .assignmentId(invalidAssignmentId)
                .subjectType(SubjectType.MATH.name())
                .gradeDate(LocalDate.now())
                .studentGrades(Map.of(student1.getId().toString(), "5"))
                .build();

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            gradeService.saveGrades(saveGradesDto, teacher);
        });
        assertTrue(exception.getMessage().contains("Invalid grade type") || 
                   exception.getMessage().contains("grade type"));
    }
    
    private boolean isValidGradeTypeUuid(UUID uuid) {
        for (GradeType gradeType : GradeType.values()) {
            if (getGradeTypeUuid(gradeType).equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    @Test
    void saveGrades_whenSubjectTypeIsNull_thenThrowIllegalArgumentException() {
        // Given
        UUID testAssignmentId = getGradeTypeUuid(GradeType.TEST);
        SaveGradesDto saveGradesDto = SaveGradesDto.builder()
                .courseId(course.getId())
                .assignmentId(testAssignmentId)
                .subjectType(null)
                .gradeDate(LocalDate.now())
                .studentGrades(Map.of(student1.getId().toString(), "5"))
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            gradeService.saveGrades(saveGradesDto, teacher);
        });
    }

    @Test
    void saveGrades_whenInvalidSubjectType_thenThrowIllegalArgumentException() {
        // Given
        UUID testAssignmentId = getGradeTypeUuid(GradeType.TEST);
        SaveGradesDto saveGradesDto = SaveGradesDto.builder()
                .courseId(course.getId())
                .assignmentId(testAssignmentId)
                .subjectType("INVALID_SUBJECT")
                .gradeDate(LocalDate.now())
                .studentGrades(Map.of(student1.getId().toString(), "5"))
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            gradeService.saveGrades(saveGradesDto, teacher);
        });
    }

    @Test
    void saveGrades_whenSubjectNotInCourse_thenThrowIllegalArgumentException() {
        // Given - course has MATH and ENGLISH, but we try to save SPORT
        UUID testAssignmentId = getGradeTypeUuid(GradeType.TEST);
        SaveGradesDto saveGradesDto = SaveGradesDto.builder()
                .courseId(course.getId())
                .assignmentId(testAssignmentId)
                .subjectType(SubjectType.SPORT.name())
                .gradeDate(LocalDate.now())
                .studentGrades(Map.of(student1.getId().toString(), "5"))
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            gradeService.saveGrades(saveGradesDto, teacher);
        });
    }

    @Test
    void saveGrades_whenStudentNotFound_thenThrowResourceNotFoundException() {
        // Given
        UUID nonExistentStudentId = UUID.randomUUID();
        UUID testAssignmentId = getGradeTypeUuid(GradeType.TEST);
        SaveGradesDto saveGradesDto = SaveGradesDto.builder()
                .courseId(course.getId())
                .assignmentId(testAssignmentId)
                .subjectType(SubjectType.MATH.name())
                .gradeDate(LocalDate.now())
                .studentGrades(Map.of(nonExistentStudentId.toString(), "5"))
                .build();

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            gradeService.saveGrades(saveGradesDto, teacher);
        });
    }

    @Test
    void saveGrades_whenInvalidGradeValue_thenSkipGrade() {
        // Given
        UUID testAssignmentId = getGradeTypeUuid(GradeType.TEST);
        Map<String, String> studentGrades = new HashMap<>();
        studentGrades.put(student1.getId().toString(), "5"); // Valid
        studentGrades.put(student2.getId().toString(), "7"); // Invalid (should be skipped)
        studentGrades.put(student3.getId().toString(), "1"); // Invalid (should be skipped)

        SaveGradesDto saveGradesDto = SaveGradesDto.builder()
                .courseId(course.getId())
                .assignmentId(testAssignmentId)
                .subjectType(SubjectType.MATH.name())
                .gradeDate(LocalDate.now())
                .studentGrades(studentGrades)
                .build();

        // When
        gradeService.saveGrades(saveGradesDto, teacher);
        entityManager.flush();
        entityManager.clear();

        // Then - only one grade should be saved
        List<Grade> savedGrades = gradeRepository.findAll();
        assertEquals(1, savedGrades.size());
        assertEquals(GradeLetter.VERY_GOOD, savedGrades.get(0).getGradeLetter());
        assertEquals(student1.getId(), savedGrades.get(0).getStudent().getId());
    }

    @Test
    void saveGrades_whenMultipleGradeTypes_thenSaveCorrectly() {
        // Given - save TEST grades first
        UUID testAssignmentId = getGradeTypeUuid(GradeType.TEST);
        SaveGradesDto testGradesDto = SaveGradesDto.builder()
                .courseId(course.getId())
                .assignmentId(testAssignmentId)
                .subjectType(SubjectType.MATH.name())
                .gradeDate(LocalDate.now())
                .studentGrades(Map.of(student1.getId().toString(), "5"))
                .build();
        gradeService.saveGrades(testGradesDto, teacher);
        entityManager.flush();

        // Then save PROJECT grades for the same subject
        UUID projectAssignmentId = getGradeTypeUuid(GradeType.PROJECT);
        SaveGradesDto projectGradesDto = SaveGradesDto.builder()
                .courseId(course.getId())
                .assignmentId(projectAssignmentId)
                .subjectType(SubjectType.MATH.name())
                .gradeDate(LocalDate.now())
                .studentGrades(Map.of(student1.getId().toString(), "6"))
                .build();
        gradeService.saveGrades(projectGradesDto, teacher);
        entityManager.flush();
        entityManager.clear();

        // Then - should have 2 grades for the same student and subject but different types
        User reloadedStudent1 = userRepository.findById(student1.getId()).orElse(null);
        List<Grade> student1Grades = gradeRepository.findByStudent(reloadedStudent1);
        assertEquals(2, student1Grades.size());

        Grade testGrade = student1Grades.stream()
                .filter(g -> g.getGradeType() == GradeType.TEST)
                .findFirst()
                .orElse(null);
        assertNotNull(testGrade);
        assertEquals(GradeLetter.VERY_GOOD, testGrade.getGradeLetter());

        Grade projectGrade = student1Grades.stream()
                .filter(g -> g.getGradeType() == GradeType.PROJECT)
                .findFirst()
                .orElse(null);
        assertNotNull(projectGrade);
        assertEquals(GradeLetter.EXCELLENT, projectGrade.getGradeLetter());
    }

    @Test
    void saveGrades_whenMixedOperations_thenHandleCorrectly() {
        // Given - create existing grade for student1
        Grade existingGrade = Grade.builder()
                .student(student1)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.AVERAGE)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now())
                .build();
        gradeRepository.save(existingGrade);
        entityManager.flush();
        UUID existingGradeId = existingGrade.getId();

        UUID testAssignmentId = getGradeTypeUuid(GradeType.TEST);
        Map<String, String> studentGrades = new HashMap<>();
        studentGrades.put(student1.getId().toString(), "6"); // Update existing
        studentGrades.put(student2.getId().toString(), "5"); // Create new
        studentGrades.put(student3.getId().toString(), ""); // Delete (no existing, so nothing happens)

        SaveGradesDto saveGradesDto = SaveGradesDto.builder()
                .courseId(course.getId())
                .assignmentId(testAssignmentId)
                .subjectType(SubjectType.MATH.name())
                .gradeDate(LocalDate.now())
                .studentGrades(studentGrades)
                .build();

        // When
        gradeService.saveGrades(saveGradesDto, teacher);
        entityManager.flush();
        entityManager.clear();

        // Then - reload students to get fresh entities
        User reloadedStudent2 = userRepository.findById(student2.getId()).orElse(null);
        
        List<Grade> allGrades = gradeRepository.findAll();
        assertEquals(2, allGrades.size()); // student1 updated, student2 new, student3 no change

        Grade updatedGrade = gradeRepository.findById(existingGradeId).orElse(null);
        assertNotNull(updatedGrade);
        assertEquals(GradeLetter.EXCELLENT, updatedGrade.getGradeLetter());

        Grade newGrade = gradeRepository.findByStudent(reloadedStudent2).stream()
                .filter(g -> g.getSubjectType() == SubjectType.MATH && g.getGradeType() == GradeType.TEST)
                .findFirst()
                .orElse(null);
        assertNotNull(newGrade);
        assertEquals(GradeLetter.VERY_GOOD, newGrade.getGradeLetter());
    }

    /**
     * Helper method to get the UUID for a GradeType.
     * The GradeService uses GradeTypeOptionDto which generates UUID using nameUUIDFromBytes.
     * This matches the exact logic in GradeTypeOptionDto.generateStableId()
     */
    private UUID getGradeTypeUuid(GradeType gradeType) {
        return UUID.nameUUIDFromBytes(gradeType.name().getBytes(StandardCharsets.UTF_8));
    }
}
