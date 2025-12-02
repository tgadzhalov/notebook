package com.example.OnlineNotebook.APITest.AdminController;

import com.example.OnlineNotebook.controller.AdminController;
import com.example.OnlineNotebook.security.AuthRoleHandler;
import com.example.OnlineNotebook.services.CourseService;
import com.example.OnlineNotebook.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class GetTeachersApiTest {

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
    private CourseService courseService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTeachers_whenValidRequest_thenReturnTeachers() throws Exception {
        List<Map<String, Object>> teachers = new ArrayList<>();
        Map<String, Object> teacher1 = new HashMap<>();
        teacher1.put("id", "123");
        teacher1.put("firstName", "John");
        teacher1.put("lastName", "Doe");
        teacher1.put("email", "john.doe@example.com");
        teachers.add(teacher1);

        Map<String, Object> teacher2 = new HashMap<>();
        teacher2.put("id", "456");
        teacher2.put("firstName", "Jane");
        teacher2.put("lastName", "Smith");
        teacher2.put("email", "jane.smith@example.com");
        teachers.add(teacher2);

        when(courseService.getTeachersList()).thenReturn(teachers);

        mockMvc.perform(get("/api/v1/admin/teachers")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"))
                .andExpect(jsonPath("$[1].firstName").value("Jane"))
                .andExpect(jsonPath("$[1].email").value("jane.smith@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTeachers_whenEmptyList_thenReturnEmptyArray() throws Exception {
        List<Map<String, Object>> teachers = new ArrayList<>();
        when(courseService.getTeachersList()).thenReturn(teachers);

        mockMvc.perform(get("/api/v1/admin/teachers")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getTeachers_whenNotAdmin_thenReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/teachers")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}




