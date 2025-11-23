package com.example.OnlineNotebook.services;

import com.example.OnlineNotebook.exceptions.ResourceNotFoundException;
import com.example.OnlineNotebook.models.dtos.admin.BulkImportUserDto;
import com.example.OnlineNotebook.models.dtos.admin.BulkRegistrationResult;
import com.example.OnlineNotebook.models.dtos.auth.EditProfileDto;
import com.example.OnlineNotebook.models.dtos.auth.RegisterDto;
import com.example.OnlineNotebook.models.dtos.admin.UserRegistrationResult;
import com.example.OnlineNotebook.models.entities.Course;
import com.example.OnlineNotebook.models.entities.User;
import com.example.OnlineNotebook.models.enums.UserType;
import com.example.OnlineNotebook.repositories.AssignmentRepository;
import com.example.OnlineNotebook.repositories.CourseRepository;
import com.example.OnlineNotebook.repositories.UserRepository;
import com.example.OnlineNotebook.security.UserData;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CourseRepository courseRepository;
    private final AssignmentRepository assignmentRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, CourseRepository courseRepository, AssignmentRepository assignmentRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.courseRepository = courseRepository;
        this.assignmentRepository = assignmentRepository;
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public void registerUser(RegisterDto registerDto) {
        if (emailExists(registerDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + registerDto.getEmail());
        }

        User user = User.builder()
                .firstName(registerDto.getFirstName())
                .lastName(registerDto.getLastName())
                .password(passwordEncoder.encode(registerDto.getPassword()))
                .email(registerDto.getEmail())
                .userType(registerDto.getUserType())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
    }

    public UserRegistrationResult registerUserWithResult(RegisterDto registerDto) {
        registerUser(registerDto);
        return UserRegistrationResult.builder()
                .success(true)
                .message("User created successfully")
                .build();
    }

    @Transactional
    public void registerUserFromBulkImport(BulkImportUserDto bulkImportDto) {
        if (emailExists(bulkImportDto.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + bulkImportDto.getEmail());
        }

        User user = User.builder()
                .firstName(bulkImportDto.getFirstName())
                .lastName(bulkImportDto.getLastName())
                .password(passwordEncoder.encode(bulkImportDto.getPassword()))
                .email(bulkImportDto.getEmail())
                .userType(bulkImportDto.getUserType())
                .studentClass(bulkImportDto.getStudentClass())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        String studentClass = bulkImportDto.getStudentClass();
        if (studentClass != null && !studentClass.isEmpty()) {
            Course course = findCourseByStudentClass(studentClass);
            user.setCourse(course);
        }

        userRepository.save(user);
    }

    private Course findCourseByStudentClass(String studentClass) {
        return courseRepository.findByName(studentClass);
    }

    public BulkRegistrationResult registerBulkUsers(List<BulkImportUserDto> users) {
        int successCount = 0;
        int failureCount = 0;

        for (BulkImportUserDto bulkImportDto : users) {
            try {
                registerUserFromBulkImport(bulkImportDto);
                successCount++;
            } catch (Exception e) {
                failureCount++;
            }
        }

        String message = String.format("Processed %d users: %d successful, %d failed", users.size(), successCount, failureCount);
        
        return BulkRegistrationResult.builder()
                .success(failureCount == 0)
                .message(message)
                .build();
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Email not found: " + email));
        return new UserData(user.getId(), email, user.getPassword(), user.getUserType());
    }

    public User getById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    public List<Map<String, Object>> getRecentUsers(int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        return userRepository.findAll(pageRequest).getContent().stream()
                .map(user -> {
                    Map<String, Object> userMap = new java.util.HashMap<>();
                    userMap.put("id", user.getId().toString());
                    userMap.put("firstName", user.getFirstName());
                    userMap.put("lastName", user.getLastName());
                    userMap.put("email", user.getEmail());
                    userMap.put("userType", user.getUserType().toString());
                    userMap.put("createdAt", user.getCreatedAt().format(formatter));
                    return userMap;
                })
                .collect(Collectors.toList());
    }
    
    public List<User> getStudentsByCourse(Course course) {
        return userRepository.findByCourse(course).stream()
                .filter(user -> user.getUserType() == UserType.STUDENT)
                .collect(Collectors.toList());
    }

    public long getTotalStudentsCount() {
        return userRepository.findByUserType(UserType.STUDENT).size();
    }

    public long getActiveClassesCount() {
        return courseRepository.count();
    }

    public long getPostedAssignmentsCount() {
        return assignmentRepository.count();
    }

    public EditProfileDto buildEditProfileDto(UUID userId) {
        User user = getById(userId);
        return EditProfileDto.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profilePictureUrl(user.getProfilePictureUrl())
                .build();
    }

    @Transactional
    public void updateUserProfile(UUID userId, EditProfileDto editProfileDto) {
        User user = getById(userId);

        validatePasswordChange(editProfileDto, user);

        if (editProfileDto.getFirstName() != null && !editProfileDto.getFirstName().trim().isEmpty()) {
            user.setFirstName(editProfileDto.getFirstName().trim());
        }

        if (editProfileDto.getLastName() != null && !editProfileDto.getLastName().trim().isEmpty()) {
            user.setLastName(editProfileDto.getLastName().trim());
        }

        if (editProfileDto.getProfilePictureUrl() != null) {
            String url = editProfileDto.getProfilePictureUrl().trim();
            user.setProfilePictureUrl(url.isEmpty() ? null : url);
        }

        if (editProfileDto.getNewPassword() != null && !editProfileDto.getNewPassword().isEmpty()) {
            if (editProfileDto.getCurrentPassword() == null || editProfileDto.getCurrentPassword().isEmpty()) {
                throw new IllegalArgumentException("Current password is required to change password");
            }

            if (!passwordEncoder.matches(editProfileDto.getCurrentPassword(), user.getPassword())) {
                throw new IllegalArgumentException("Current password is incorrect");
            }

            if (passwordEncoder.matches(editProfileDto.getNewPassword(), user.getPassword())) {
                throw new IllegalArgumentException("New password must be different from current password");
            }

            user.setPassword(passwordEncoder.encode(editProfileDto.getNewPassword()));
        }

        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private void validatePasswordChange(EditProfileDto editProfileDto, User user) {
        if (editProfileDto.getNewPassword() != null && !editProfileDto.getNewPassword().isEmpty()) {
            if (!editProfileDto.getNewPassword().equals(editProfileDto.getConfirmPassword())) {
                throw new IllegalArgumentException("New password and confirm password do not match");
            }
        }
    }
}
