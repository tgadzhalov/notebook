package com.example.OnlineNotebook.IntegrationTest.CourseService;

import com.example.OnlineNotebook.exceptions.ResourceNotFoundException;
import com.example.OnlineNotebook.models.dtos.course.CourseDto;
import com.example.OnlineNotebook.models.entities.Course;
import com.example.OnlineNotebook.models.entities.User;
import com.example.OnlineNotebook.models.enums.SubjectType;
import com.example.OnlineNotebook.models.enums.UserType;
import com.example.OnlineNotebook.repositories.CourseRepository;
import com.example.OnlineNotebook.repositories.UserRepository;
import com.example.OnlineNotebook.services.CourseService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CourseServiceITest {

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

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
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void createCourse_whenValidData_thenShouldCreateCourse() {
        CourseDto courseDto = CourseDto.builder()
                .name("Test Kurs")
                .description("Opisanie na kurs")
                .schoolYear("2024-2025")
                .teacherId(teacher.getId())
                .subjects(Arrays.asList(SubjectType.MATH, SubjectType.SCIENCE))
                .build();

        Course result = courseService.createCourse(courseDto);
        entityManager.flush();
        entityManager.clear();

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Test Kurs", result.getName());
        assertEquals("Opisanie na kurs", result.getDescription());
        assertEquals("2024-2025", result.getSchoolYear());
        assertEquals(teacher.getId(), result.getTeacher().getId());
        assertEquals(2, result.getSubjects().size());
        assertTrue(result.getSubjects().contains(SubjectType.MATH));
        assertTrue(result.getSubjects().contains(SubjectType.SCIENCE));

        Course savedCourse = courseRepository.findById(result.getId()).orElse(null);
        assertNotNull(savedCourse);
        assertEquals("Test Kurs", savedCourse.getName());
    }

    @Test
    void createCourse_whenTeacherNotFound_thenShouldThrowException() {
        UUID nonExistentTeacherId = UUID.randomUUID();
        CourseDto courseDto = CourseDto.builder()
                .name("Test Kurs")
                .description("Opisanie")
                .schoolYear("2024-2025")
                .teacherId(nonExistentTeacherId)
                .build();

        assertThrows(ResourceNotFoundException.class, () -> {
            courseService.createCourse(courseDto);
        });
    }

    @Test
    void createCourse_whenUserNotTeacher_thenShouldThrowException() {
        User student = User.builder()
                .firstName("Petar")
                .lastName("Petrov")
                .email("petar.petrov@example.com")
                .password(passwordEncoder.encode("password123"))
                .userType(UserType.STUDENT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        student = userRepository.save(student);
        entityManager.flush();

        CourseDto courseDto = CourseDto.builder()
                .name("Test Kurs")
                .description("Opisanie")
                .schoolYear("2024-2025")
                .teacherId(student.getId())
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            courseService.createCourse(courseDto);
        });
    }

    @Test
    void createCourse_whenSubjectsIsNull_thenShouldCreateWithEmptySubjects() {
        CourseDto courseDto = CourseDto.builder()
                .name("Test Kurs")
                .description("Opisanie")
                .schoolYear("2024-2025")
                .teacherId(teacher.getId())
                .subjects(null)
                .build();

        Course result = courseService.createCourse(courseDto);
        entityManager.flush();
        entityManager.clear();

        assertNotNull(result);
        assertNotNull(result.getSubjects());
        assertTrue(result.getSubjects().isEmpty());
    }

    @Test
    void getTeachers_whenTeachersExist_thenShouldReturnAllTeachers() {
        final User teacher2 = User.builder()
                .firstName("Krum")
                .lastName("Krumov")
                .email("krum.krumov@example.com")
                .password(passwordEncoder.encode("password123"))
                .userType(UserType.TEACHER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        final UUID teacher2Id = userRepository.save(teacher2).getId();
        entityManager.flush();
        entityManager.clear();

        List<User> teachers = courseService.getTeachers();

        assertNotNull(teachers);
        assertTrue(teachers.size() >= 2);
        assertTrue(teachers.stream().anyMatch(t -> t.getId().equals(teacher.getId())));
        assertTrue(teachers.stream().anyMatch(t -> t.getId().equals(teacher2Id)));
    }

    @Test
    void getCourseById_whenCourseExists_thenShouldReturnCourse() {
        Course course = Course.builder()
                .name("Test Kurs")
                .description("Opisanie")
                .schoolYear("2024-2025")
                .teacher(teacher)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        course.setSubjects(Arrays.asList(SubjectType.MATH));
        course = courseRepository.save(course);
        entityManager.flush();
        entityManager.clear();

        Course result = courseService.getCourseById(course.getId());

        assertNotNull(result);
        assertEquals(course.getId(), result.getId());
        assertEquals("Test Kurs", result.getName());
    }

    @Test
    void getCourseById_whenCourseNotFound_thenShouldReturnNull() {
        UUID nonExistentId = UUID.randomUUID();

        Course result = courseService.getCourseById(nonExistentId);

        assertNull(result);
    }

    @Test
    void getCoursesByTeacher_whenTeacherHasCourses_thenShouldReturnCourses() {
        final Course course1 = Course.builder()
                .name("Test Kurs 1")
                .description("Opisanie 1")
                .schoolYear("2024-2025")
                .teacher(teacher)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        final UUID course1Id = courseRepository.save(course1).getId();

        final Course course2 = Course.builder()
                .name("Test Kurs 2")
                .description("Opisanie 2")
                .schoolYear("2024-2025")
                .teacher(teacher)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        final UUID course2Id = courseRepository.save(course2).getId();
        entityManager.flush();
        entityManager.clear();

        teacher = userRepository.findById(teacher.getId()).orElse(null);
        List<Course> courses = courseService.getCoursesByTeacher(teacher);

        assertNotNull(courses);
        assertTrue(courses.size() >= 2);
        assertTrue(courses.stream().anyMatch(c -> c.getId().equals(course1Id)));
        assertTrue(courses.stream().anyMatch(c -> c.getId().equals(course2Id)));
    }

    @Test
    void getCoursesByTeacher_whenTeacherHasNoCourses_thenShouldReturnEmptyList() {
        User newTeacher = User.builder()
                .firstName("Georgi")
                .lastName("Georgiev")
                .email("georgi.georgiev@example.com")
                .password(passwordEncoder.encode("password123"))
                .userType(UserType.TEACHER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        newTeacher = userRepository.save(newTeacher);
        entityManager.flush();
        entityManager.clear();

        newTeacher = userRepository.findById(newTeacher.getId()).orElse(null);
        List<Course> courses = courseService.getCoursesByTeacher(newTeacher);

        assertNotNull(courses);
        assertTrue(courses.isEmpty());
    }

    @Test
    void getTeachersList_whenTeachersExist_thenShouldReturnListWithMaps() {
        final User teacher2 = User.builder()
                .firstName("Krum")
                .lastName("Krumov")
                .email("krum.krumov@example.com")
                .password(passwordEncoder.encode("password123"))
                .userType(UserType.TEACHER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        final UUID teacher2Id = userRepository.save(teacher2).getId();
        entityManager.flush();
        entityManager.clear();

        List<Map<String, Object>> teachersList = courseService.getTeachersList();

        assertNotNull(teachersList);
        assertTrue(teachersList.size() >= 2);
        
        Map<String, Object> teacherMap = teachersList.stream()
                .filter(t -> t.get("id").equals(teacher.getId().toString()))
                .findFirst()
                .orElse(null);
        
        assertNotNull(teacherMap);
        assertEquals(teacher.getId().toString(), teacherMap.get("id"));
        assertEquals(teacher.getFirstName(), teacherMap.get("firstName"));
        assertEquals(teacher.getLastName(), teacherMap.get("lastName"));
        assertEquals(teacher.getEmail(), teacherMap.get("email"));
    }
}

