package com.example.OnlineNotebook.APITest.AdminController;

import com.example.OnlineNotebook.controller.AdminController;
import com.example.OnlineNotebook.models.dtos.course.CourseDto;
import com.example.OnlineNotebook.models.entities.Course;
import com.example.OnlineNotebook.models.entities.User;
import com.example.OnlineNotebook.models.enums.UserType;
import com.example.OnlineNotebook.security.AuthRoleHandler;
import com.example.OnlineNotebook.services.CourseService;
import com.example.OnlineNotebook.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class CreateCourseApiTest {

    @TestConfiguration
    static class MockConfiguration {
        @Bean
        public UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        public CourseService courseService() {
            return mock(CourseService.class);
        }

        @Bean
        public AuthRoleHandler authRoleHandler() {
            return mock(AuthRoleHandler.class);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CourseService courseService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCourse_whenValidData_thenReturnSuccess() throws Exception {
        UUID teacherId = UUID.randomUUID();
        CourseDto courseDto = CourseDto.builder()
                .name("Mathematics")
                .description("Advanced Mathematics Course")
                .schoolYear("2024-2025")
                .teacherId(teacherId)
                .build();

        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("Mathematics")
                .description("Advanced Mathematics Course")
                .schoolYear("2024-2025")
                .teacher(User.builder().id(teacherId).userType(UserType.TEACHER).build())
                .build();

        when(courseService.createCourse(any(CourseDto.class))).thenReturn(course);

        mockMvc.perform(post("/api/v1/admin/courses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Course created successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCourse_whenInvalidData_thenReturnBadRequest() throws Exception {
        CourseDto courseDto = CourseDto.builder()
                .name("")  // Invalid: empty name
                .description("Advanced Mathematics Course")
                .schoolYear("")  // Invalid: empty school year
                .teacherId(null)  // Invalid: null teacher ID
                .build();

        mockMvc.perform(post("/api/v1/admin/courses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCourse_whenNameTooShort_thenReturnBadRequest() throws Exception {
        UUID teacherId = UUID.randomUUID();
        CourseDto courseDto = CourseDto.builder()
                .name("A")  // Invalid: too short (min 2)
                .description("Advanced Mathematics Course")
                .schoolYear("2024-2025")
                .teacherId(teacherId)
                .build();

        mockMvc.perform(post("/api/v1/admin/courses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createCourse_whenNotAdmin_thenReturnForbidden() throws Exception {
        UUID teacherId = UUID.randomUUID();
        CourseDto courseDto = CourseDto.builder()
                .name("Mathematics")
                .description("Advanced Mathematics Course")
                .schoolYear("2024-2025")
                .teacherId(teacherId)
                .build();

        mockMvc.perform(post("/api/v1/admin/courses")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courseDto)))
                .andExpect(status().isForbidden());
    }
}
