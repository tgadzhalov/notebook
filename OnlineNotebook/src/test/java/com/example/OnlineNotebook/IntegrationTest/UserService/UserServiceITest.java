package com.example.OnlineNotebook.IntegrationTest.UserService;

import com.example.OnlineNotebook.exceptions.ResourceNotFoundException;
import com.example.OnlineNotebook.models.dtos.admin.BulkImportUserDto;
import com.example.OnlineNotebook.models.dtos.auth.EditProfileDto;
import com.example.OnlineNotebook.models.dtos.auth.RegisterDto;
import com.example.OnlineNotebook.models.entities.Assignment;
import com.example.OnlineNotebook.models.entities.Course;
import com.example.OnlineNotebook.models.entities.User;
import com.example.OnlineNotebook.models.enums.UserType;
import com.example.OnlineNotebook.repositories.AssignmentRepository;
import com.example.OnlineNotebook.repositories.CourseRepository;
import com.example.OnlineNotebook.repositories.UserRepository;
import com.example.OnlineNotebook.security.UserData;
import com.example.OnlineNotebook.services.UserService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class UserServiceITest {

    @Autowired
    private UserService userService;

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

    private User existingUser;
    private Course course;

    @BeforeEach
    void setUp() {
        existingUser = User.builder()
                .firstName("Ivan")
                .lastName("Blagoev")
                .email("ivan@test.com")
                .password(passwordEncoder.encode("password123"))
                .userType(UserType.STUDENT)
                .build();
        existingUser = userRepository.save(existingUser);

        User teacher = User.builder()
                .firstName("Krum")
                .lastName("Krumov")
                .email("krum@test.com")
                .password(passwordEncoder.encode("password"))
                .userType(UserType.TEACHER)
                .build();
        teacher = userRepository.save(teacher);

        course = Course.builder()
                .name("10A")
                .description("Test Course")
                .schoolYear("2024-2025")
                .teacher(teacher)
                .build();
        course = courseRepository.save(course);
    }

    @Test
    void emailExists_whenEmailExists_thenReturnTrue() {
        boolean exists = userService.emailExists("ivan@test.com");

        assertTrue(exists);
    }

    @Test
    void emailExists_whenEmailNotExists_thenReturnFalse() {
        boolean exists = userService.emailExists("nonexistent@test.com");

        assertFalse(exists);
    }

    @Test
    void registerUser_whenValidData_thenCreateUser() {
        RegisterDto registerDto = RegisterDto.builder()
                .firstName("Petar")
                .lastName("Petrov")
                .email("petar@test.com")
                .password("password123")
                .userType(UserType.STUDENT)
                .build();

        userService.registerUser(registerDto);
        entityManager.flush();
        entityManager.clear();

        User savedUser = userRepository.findByEmail("petar@test.com").orElse(null);
        assertNotNull(savedUser);
        assertEquals("Petar", savedUser.getFirstName());
        assertEquals("Petrov", savedUser.getLastName());
        assertEquals(UserType.STUDENT, savedUser.getUserType());
        assertTrue(passwordEncoder.matches("password123", savedUser.getPassword()));
    }

    @Test
    void registerUser_whenEmailExists_thenThrowException() {
        RegisterDto registerDto = RegisterDto.builder()
                .firstName("Petar")
                .lastName("Petrov")
                .email("ivan@test.com")
                .password("password123")
                .userType(UserType.STUDENT)
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUser(registerDto);
        });
    }

    @Test
    void registerUserWithResult_whenValidData_thenReturnSuccess() {
        RegisterDto registerDto = RegisterDto.builder()
                .firstName("Georgi")
                .lastName("Georgiev")
                .email("georgi@test.com")
                .password("password123")
                .userType(UserType.TEACHER)
                .build();

        var result = userService.registerUserWithResult(registerDto);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getMessage());
    }

    @Test
    void registerUserFromBulkImport_whenValidData_thenCreateUser() {
        BulkImportUserDto bulkDto = BulkImportUserDto.builder()
                .firstName("Nikolay")
                .lastName("Nikolov")
                .email("nikolay@test.com")
                .password("password123")
                .userType(UserType.STUDENT)
                .studentClass("10A")
                .build();

        userService.registerUserFromBulkImport(bulkDto);
        entityManager.flush();
        entityManager.clear();

        User savedUser = userRepository.findByEmail("nikolay@test.com").orElse(null);
        assertNotNull(savedUser);
        assertEquals("Nikolay", savedUser.getFirstName());
        assertEquals("10A", savedUser.getStudentClass());
        assertNotNull(savedUser.getCourse());
        assertEquals(course.getId(), savedUser.getCourse().getId());
    }

    @Test
    void registerUserFromBulkImport_whenNoStudentClass_thenCreateUserWithoutCourse() {
        BulkImportUserDto bulkDto = BulkImportUserDto.builder()
                .firstName("Dimitar")
                .lastName("Dimitrov")
                .email("dimitar@test.com")
                .password("password123")
                .userType(UserType.STUDENT)
                .studentClass(null)
                .build();

        userService.registerUserFromBulkImport(bulkDto);
        entityManager.flush();
        entityManager.clear();

        User savedUser = userRepository.findByEmail("dimitar@test.com").orElse(null);
        assertNotNull(savedUser);
        assertNull(savedUser.getCourse());
    }

    @Test
    void registerUserFromBulkImport_whenEmailExists_thenThrowException() {
        BulkImportUserDto bulkDto = BulkImportUserDto.builder()
                .firstName("Test")
                .lastName("User")
                .email("ivan@test.com")
                .password("password123")
                .userType(UserType.STUDENT)
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            userService.registerUserFromBulkImport(bulkDto);
        });
    }

    @Test
    void registerBulkUsers_whenValidUsers_thenProcessAll() {
        List<BulkImportUserDto> users = List.of(
                BulkImportUserDto.builder()
                        .firstName("Stoyan")
                        .lastName("Stoyanov")
                        .email("stoyan@test.com")
                        .password("password123")
                        .userType(UserType.STUDENT)
                        .build(),
                BulkImportUserDto.builder()
                        .firstName("Vasil")
                        .lastName("Vasilev")
                        .email("vasil@test.com")
                        .password("password123")
                        .userType(UserType.STUDENT)
                        .build()
        );

        var result = userService.registerBulkUsers(users);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals(2, userRepository.findByEmail("stoyan@test.com").map(u -> 1).orElse(0) +
                userRepository.findByEmail("vasil@test.com").map(u -> 1).orElse(0));
    }

    @Test
    void registerBulkUsers_whenSomeFail_thenReturnPartialSuccess() {
        List<BulkImportUserDto> users = List.of(
                BulkImportUserDto.builder()
                        .firstName("Stoyan")
                        .lastName("Stoyanov")
                        .email("stoyan@test.com")
                        .password("password123")
                        .userType(UserType.STUDENT)
                        .build(),
                BulkImportUserDto.builder()
                        .firstName("Vasil")
                        .lastName("Vasilev")
                        .email("ivan@test.com")
                        .password("password123")
                        .userType(UserType.STUDENT)
                        .build()
        );

        var result = userService.registerBulkUsers(users);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("failed"));
    }

    @Test
    void loadUserByUsername_whenUserExists_thenReturnUserDetails() {
        UserDetails userDetails = userService.loadUserByUsername("ivan@test.com");

        assertNotNull(userDetails);
        assertTrue(userDetails instanceof UserData);
        assertEquals("ivan@test.com", userDetails.getUsername());
    }

    @Test
    void loadUserByUsername_whenUserNotExists_thenThrowException() {
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.loadUserByUsername("nonexistent@test.com");
        });
    }

    @Test
    void getById_whenUserExists_thenReturnUser() {
        User user = userService.getById(existingUser.getId());

        assertNotNull(user);
        assertEquals(existingUser.getId(), user.getId());
        assertEquals("ivan@test.com", user.getEmail());
    }

    @Test
    void getById_whenUserNotExists_thenThrowException() {
        UUID nonExistentId = UUID.randomUUID();

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getById(nonExistentId);
        });
    }

    @Test
    void getRecentUsers_whenUsersExist_thenReturnLimitedList() {
        for (int i = 0; i < 5; i++) {
            User user = User.builder()
                    .firstName("User" + i)
                    .lastName("LastName" + i)
                    .email("user" + i + "@test.com")
                    .password(passwordEncoder.encode("password"))
                    .userType(UserType.STUDENT)
                    .build();
            userRepository.save(user);
        }
        entityManager.flush();

        List<Map<String, Object>> recentUsers = userService.getRecentUsers(3);

        assertNotNull(recentUsers);
        assertEquals(3, recentUsers.size());
    }

    @Test
    void getStudentsByCourse_whenCourseHasStudents_thenReturnStudents() {
        User student1 = User.builder()
                .firstName("Boris")
                .lastName("Borisov")
                .email("boris@test.com")
                .password(passwordEncoder.encode("password"))
                .userType(UserType.STUDENT)
                .course(course)
                .build();
        userRepository.save(student1);

        User student2 = User.builder()
                .firstName("Todor")
                .lastName("Todorov")
                .email("todor@test.com")
                .password(passwordEncoder.encode("password"))
                .userType(UserType.STUDENT)
                .course(course)
                .build();
        userRepository.save(student2);

        User teacher = User.builder()
                .firstName("Teacher")
                .lastName("Name")
                .email("teacher2@test.com")
                .password(passwordEncoder.encode("password"))
                .userType(UserType.TEACHER)
                .course(course)
                .build();
        userRepository.save(teacher);
        entityManager.flush();
        entityManager.clear();

        List<User> students = userService.getStudentsByCourse(course);

        assertNotNull(students);
        assertEquals(2, students.size());
        assertTrue(students.stream().allMatch(s -> s.getUserType() == UserType.STUDENT));
    }

    @Test
    void getTotalStudentsCount_whenStudentsExist_thenReturnCount() {
        for (int i = 0; i < 3; i++) {
            User student = User.builder()
                    .firstName("Student" + i)
                    .lastName("LastName" + i)
                    .email("student" + i + "@test.com")
                    .password(passwordEncoder.encode("password"))
                    .userType(UserType.STUDENT)
                    .build();
            userRepository.save(student);
        }
        entityManager.flush();

        long count = userService.getTotalStudentsCount();

        assertTrue(count >= 4);
    }

    @Test
    void getActiveClassesCount_whenCoursesExist_thenReturnCount() {
        User teacher = userRepository.findByEmail("krum@test.com").orElse(null);
        if (teacher == null) {
            teacher = User.builder()
                    .firstName("Krum")
                    .lastName("Krumov")
                    .email("krum@test.com")
                    .password(passwordEncoder.encode("password"))
                    .userType(UserType.TEACHER)
                    .build();
            teacher = userRepository.save(teacher);
        }

        Course course2 = Course.builder()
                .name("11A")
                .description("Other Course")
                .schoolYear("2024-2025")
                .teacher(teacher)
                .build();
        courseRepository.save(course2);
        entityManager.flush();

        long count = userService.getActiveClassesCount();

        assertTrue(count >= 2);
    }

    @Test
    void getPostedAssignmentsCount_whenAssignmentsExist_thenReturnCount() {
        User teacher = course.getTeacher();
        LocalDateTime now = LocalDateTime.now();
        Assignment assignment1 = Assignment.builder()
                .title("Assignment 1")
                .description("Test")
                .course(course)
                .createdBy(teacher)
                .dueDate(now.plusDays(7))
                .assignedDate(now)
                .build();
        assignmentRepository.save(assignment1);

        Assignment assignment2 = Assignment.builder()
                .title("Assignment 2")
                .description("Test")
                .course(course)
                .createdBy(teacher)
                .dueDate(now.plusDays(7))
                .assignedDate(now)
                .build();
        assignmentRepository.save(assignment2);
        entityManager.flush();

        long count = userService.getPostedAssignmentsCount();

        assertTrue(count >= 2);
    }

    @Test
    void buildEditProfileDto_whenUserExists_thenReturnDto() {
        var dto = userService.buildEditProfileDto(existingUser.getId());

        assertNotNull(dto);
        assertEquals("Ivan", dto.getFirstName());
        assertEquals("Blagoev", dto.getLastName());
    }

    @Test
    void buildEditProfileDto_whenUserNotExists_thenThrowException() {
        UUID nonExistentId = UUID.randomUUID();

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.buildEditProfileDto(nonExistentId);
        });
    }

    @Test
    void updateUserProfile_whenUpdateName_thenUpdateSuccessfully() {
        EditProfileDto editDto = EditProfileDto.builder()
                .firstName("Updated")
                .lastName("Name")
                .build();

        userService.updateUserProfile(existingUser.getId(), editDto);
        entityManager.flush();
        entityManager.clear();

        User updatedUser = userRepository.findById(existingUser.getId()).orElse(null);
        assertNotNull(updatedUser);
        assertEquals("Updated", updatedUser.getFirstName());
        assertEquals("Name", updatedUser.getLastName());
    }

    @Test
    void updateUserProfile_whenUpdateProfilePicture_thenUpdateSuccessfully() {
        EditProfileDto editDto = EditProfileDto.builder()
                .firstName("Ivan")
                .lastName("Blagoev")
                .profilePictureUrl("https://example.com/picture.jpg")
                .build();

        userService.updateUserProfile(existingUser.getId(), editDto);
        entityManager.flush();
        entityManager.clear();

        User updatedUser = userRepository.findById(existingUser.getId()).orElse(null);
        assertNotNull(updatedUser);
        assertEquals("https://example.com/picture.jpg", updatedUser.getProfilePictureUrl());
    }

    @Test
    void updateUserProfile_whenEmptyProfilePicture_thenSetToNull() {
        existingUser.setProfilePictureUrl("https://example.com/picture.jpg");
        userRepository.save(existingUser);
        entityManager.flush();

        EditProfileDto editDto = EditProfileDto.builder()
                .firstName("Ivan")
                .lastName("Blagoev")
                .profilePictureUrl("   ")
                .build();

        userService.updateUserProfile(existingUser.getId(), editDto);
        entityManager.flush();
        entityManager.clear();

        User updatedUser = userRepository.findById(existingUser.getId()).orElse(null);
        assertNotNull(updatedUser);
        assertNull(updatedUser.getProfilePictureUrl());
    }

    @Test
    void updateUserProfile_whenUpdatePassword_thenUpdateSuccessfully() {
        EditProfileDto editDto = EditProfileDto.builder()
                .firstName("Ivan")
                .lastName("Blagoev")
                .currentPassword("password123")
                .newPassword("newpassword123")
                .confirmPassword("newpassword123")
                .build();

        userService.updateUserProfile(existingUser.getId(), editDto);
        entityManager.flush();
        entityManager.clear();

        User updatedUser = userRepository.findById(existingUser.getId()).orElse(null);
        assertNotNull(updatedUser);
        assertTrue(passwordEncoder.matches("newpassword123", updatedUser.getPassword()));
    }

    @Test
    void updateUserProfile_whenPasswordMismatch_thenThrowException() {
        EditProfileDto editDto = EditProfileDto.builder()
                .firstName("Ivan")
                .lastName("Blagoev")
                .currentPassword("password123")
                .newPassword("newpassword123")
                .confirmPassword("differentpassword")
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUserProfile(existingUser.getId(), editDto);
        });
    }

    @Test
    void updateUserProfile_whenCurrentPasswordIncorrect_thenThrowException() {
        EditProfileDto editDto = EditProfileDto.builder()
                .firstName("Ivan")
                .lastName("Blagoev")
                .currentPassword("wrongpassword")
                .newPassword("newpassword123")
                .confirmPassword("newpassword123")
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUserProfile(existingUser.getId(), editDto);
        });
    }

    @Test
    void updateUserProfile_whenNewPasswordSameAsCurrent_thenThrowException() {
        EditProfileDto editDto = EditProfileDto.builder()
                .firstName("Ivan")
                .lastName("Blagoev")
                .currentPassword("password123")
                .newPassword("password123")
                .confirmPassword("password123")
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUserProfile(existingUser.getId(), editDto);
        });
    }

    @Test
    void updateUserProfile_whenNewPasswordWithoutCurrent_thenThrowException() {
        EditProfileDto editDto = EditProfileDto.builder()
                .firstName("Ivan")
                .lastName("Blagoev")
                .newPassword("newpassword123")
                .confirmPassword("newpassword123")
                .build();

        assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUserProfile(existingUser.getId(), editDto);
        });
    }

    @Test
    void updateUserProfile_whenUserNotExists_thenThrowException() {
        UUID nonExistentId = UUID.randomUUID();
        EditProfileDto editDto = EditProfileDto.builder()
                .firstName("Ivan")
                .lastName("Blagoev")
                .build();

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUserProfile(nonExistentId, editDto);
        });
    }
}
