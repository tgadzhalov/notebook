package com.example.OnlineNotebook.APITest.TeacherController;

import com.example.OnlineNotebook.models.entities.Assignment;
import com.example.OnlineNotebook.models.entities.Course;
import com.example.OnlineNotebook.models.entities.User;
import com.example.OnlineNotebook.models.enums.AssignmentType;
import com.example.OnlineNotebook.models.enums.UserType;
import com.example.OnlineNotebook.repositories.AssignmentRepository;
import com.example.OnlineNotebook.repositories.CourseRepository;
import com.example.OnlineNotebook.repositories.UserRepository;
import com.example.OnlineNotebook.models.enums.UserType;
import com.example.OnlineNotebook.security.UserData;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class TeacherControllerAPITest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    private User teacher;
    private Course course;

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
        course.setSubjects(Arrays.asList(com.example.OnlineNotebook.models.enums.SubjectType.MATH));
        course = courseRepository.save(course);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void home_whenAuthenticatedTeacher_thenShouldReturnHomeView() throws Exception {
        UserData userData = UserData.builder()
                .id(teacher.getId())
                .email(teacher.getEmail())
                .password(teacher.getPassword())
                .userType(UserType.TEACHER)
                .build();

        mockMvc.perform(get("/teacher/home")
                        .with(SecurityMockMvcRequestPostProcessors.user(userData)))
                .andExpect(status().isOk())
                .andExpect(view().name("teacher/home"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("assignments"))
                .andExpect(model().attributeExists("totalStudents"))
                .andExpect(model().attributeExists("activeClasses"))
                .andExpect(model().attributeExists("postedAssignments"));
    }

    @Test
    void students_whenAuthenticatedTeacher_thenShouldReturnStudentsView() throws Exception {
        UserData userData = UserData.builder()
                .id(teacher.getId())
                .email(teacher.getEmail())
                .password(teacher.getPassword())
                .userType(UserType.TEACHER)
                .build();

        mockMvc.perform(get("/teacher/students")
                        .with(SecurityMockMvcRequestPostProcessors.user(userData)))
                .andExpect(status().isOk())
                .andExpect(view().name("teacher/students"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("courses"))
                .andExpect(model().attributeExists("students"));
    }

    @Test
    void students_whenCourseIdProvided_thenShouldFilterByCourse() throws Exception {
        UserData userData = UserData.builder()
                .id(teacher.getId())
                .email(teacher.getEmail())
                .password(teacher.getPassword())
                .userType(UserType.TEACHER)
                .build();

        mockMvc.perform(get("/teacher/students")
                        .param("courseId", course.getId().toString())
                        .with(SecurityMockMvcRequestPostProcessors.user(userData)))
                .andExpect(status().isOk())
                .andExpect(view().name("teacher/students"))
                .andExpect(model().attributeExists("selectedCourseId"));
    }

    @Test
    void students_whenStudentIdProvided_thenShouldShowGradesModal() throws Exception {
        User student = User.builder()
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

        UserData userData = UserData.builder()
                .id(teacher.getId())
                .email(teacher.getEmail())
                .password(teacher.getPassword())
                .userType(UserType.TEACHER)
                .build();

        mockMvc.perform(get("/teacher/students")
                        .param("courseId", course.getId().toString())
                        .param("studentId", student.getId().toString())
                        .with(SecurityMockMvcRequestPostProcessors.user(userData)))
                .andExpect(status().isOk())
                .andExpect(view().name("teacher/students"))
                .andExpect(model().attributeExists("selectedStudentId"));
    }

    @Test
    void schedule_whenAuthenticatedTeacher_thenShouldRedirectToHome() throws Exception {
        UserData userData = UserData.builder()
                .id(teacher.getId())
                .email(teacher.getEmail())
                .password(teacher.getPassword())
                .userType(UserType.TEACHER)
                .build();

        mockMvc.perform(get("/teacher/schedule")
                        .with(SecurityMockMvcRequestPostProcessors.user(userData)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/teacher/home"));
    }

    @Test
    void attendance_whenAuthenticatedTeacher_thenShouldReturnAttendanceView() throws Exception {
        UserData userData = UserData.builder()
                .id(teacher.getId())
                .email(teacher.getEmail())
                .password(teacher.getPassword())
                .userType(UserType.TEACHER)
                .build();

        mockMvc.perform(get("/teacher/attendance")
                        .with(SecurityMockMvcRequestPostProcessors.user(userData)))
                .andExpect(status().isOk())
                .andExpect(view().name("teacher/attendance"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("courses"));
    }

    @Test
    void attendance_whenCourseIdProvided_thenShouldFilterByCourse() throws Exception {
        UserData userData = UserData.builder()
                .id(teacher.getId())
                .email(teacher.getEmail())
                .password(teacher.getPassword())
                .userType(UserType.TEACHER)
                .build();

        mockMvc.perform(get("/teacher/attendance")
                        .param("courseId", course.getId().toString())
                        .with(SecurityMockMvcRequestPostProcessors.user(userData)))
                .andExpect(status().isOk())
                .andExpect(view().name("teacher/attendance"))
                .andExpect(model().attributeExists("selectedCourse"));
    }

    @Test
    void editProfile_whenAuthenticatedTeacher_thenShouldReturnEditProfileView() throws Exception {
        UserData userData = UserData.builder()
                .id(teacher.getId())
                .email(teacher.getEmail())
                .password(teacher.getPassword())
                .userType(UserType.TEACHER)
                .build();

        mockMvc.perform(get("/teacher/edit-profile")
                        .with(SecurityMockMvcRequestPostProcessors.user(userData)))
                .andExpect(status().isOk())
                .andExpect(view().name("teacher/edit-profile"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("editProfileDto"));
    }

    @Test
    void home_whenTeacherHasAssignments_thenShouldIncludeAssignments() throws Exception {
        Assignment assignment = Assignment.builder()
                .title("Zadanie 1")
                .description("Opisanie")
                .type(AssignmentType.HOMEWORK)
                .course(course)
                .createdBy(teacher)
                .dueDate(LocalDateTime.now().plusDays(7))
                .assignedDate(LocalDateTime.now())
                .build();
        assignment = assignmentRepository.save(assignment);
        entityManager.flush();
        entityManager.clear();

        UserData userData = UserData.builder()
                .id(teacher.getId())
                .email(teacher.getEmail())
                .password(teacher.getPassword())
                .userType(UserType.TEACHER)
                .build();

        mockMvc.perform(get("/teacher/home")
                        .with(SecurityMockMvcRequestPostProcessors.user(userData)))
                .andExpect(status().isOk())
                .andExpect(view().name("teacher/home"))
                .andExpect(model().attributeExists("assignments"));
    }
}

