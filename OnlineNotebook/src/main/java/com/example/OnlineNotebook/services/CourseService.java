package com.example.OnlineNotebook.services;

import com.example.OnlineNotebook.exceptions.ResourceNotFoundException;
import com.example.OnlineNotebook.models.dtos.course.CourseDto;
import com.example.OnlineNotebook.models.entities.Course;
import com.example.OnlineNotebook.models.entities.User;
import com.example.OnlineNotebook.models.enums.UserType;
import com.example.OnlineNotebook.repositories.CourseRepository;
import com.example.OnlineNotebook.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CourseService {
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    
    public CourseService(CourseRepository courseRepository, UserRepository userRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }
    
    @Transactional
    @CacheEvict(value = {"courses", "students", "studentHome"}, allEntries = true)
    public Course createCourse(CourseDto courseDto) {
        log.info("Creating course: {} for teacherId: {}", courseDto.getName(), courseDto.getTeacherId());
        User teacher = userRepository.findById(courseDto.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + courseDto.getTeacherId()));
        
        if (teacher.getUserType() != UserType.TEACHER) {
            throw new IllegalArgumentException("User with ID " + courseDto.getTeacherId() + " is not a teacher");
        }
        
        Course course = Course.builder()
                .name(courseDto.getName())
                .description(courseDto.getDescription())
                .schoolYear(courseDto.getSchoolYear())
                .teacher(teacher)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        course.setSubjects(courseDto.getSubjects() != null ? new ArrayList<>(courseDto.getSubjects()) : new ArrayList<>());
        
        Course savedCourse = courseRepository.save(course);
        log.info("Course created successfully with id: {}, name: {}", savedCourse.getId(), savedCourse.getName());
        return savedCourse;
    }
    
    public List<User> getTeachers() {
        return userRepository.findByUserType(UserType.TEACHER);
    }
    
    public Course getCourseById(UUID courseId) {
        return courseRepository.findById(courseId).orElse(null);
    }
    
    @Cacheable(value = "courses", key = "#teacher.id")
    public List<Course> getCoursesByTeacher(User teacher) {
        return courseRepository.findByTeacher(teacher);
    }
    
    public List<Map<String, Object>> getTeachersList() {
        return getTeachers().stream()
                .map(teacher -> {
                    Map<String, Object> teacherMap = new HashMap<>();
                    teacherMap.put("id", teacher.getId().toString());
                    teacherMap.put("firstName", teacher.getFirstName());
                    teacherMap.put("lastName", teacher.getLastName());
                    teacherMap.put("email", teacher.getEmail());
                    return teacherMap;
                })
                .collect(Collectors.toList());
    }
}
