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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class GetRecentUsersApiTest {

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
    private UserService userService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRecentUsers_whenValidRequest_thenReturnUsers() throws Exception {
        List<Map<String, Object>> users = new ArrayList<>();
        Map<String, Object> user1 = new HashMap<>();
        user1.put("id", "123");
        user1.put("firstName", "Ivan");
        user1.put("lastName", "Schweicarov");
        user1.put("email", "test@example.com");
        users.add(user1);

        when(userService.getRecentUsers(anyInt())).thenReturn(users);

        mockMvc.perform(get("/api/v1/admin/users")
                        .with(csrf())
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Ivan"))
                .andExpect(jsonPath("$[0].email").value("test@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRecentUsers_whenDefaultLimit_thenReturnUsers() throws Exception {
        List<Map<String, Object>> users = new ArrayList<>();
        when(userService.getRecentUsers(20)).thenReturn(users);

        mockMvc.perform(get("/api/v1/admin/users")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getRecentUsers_whenNotAdmin_thenReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}




