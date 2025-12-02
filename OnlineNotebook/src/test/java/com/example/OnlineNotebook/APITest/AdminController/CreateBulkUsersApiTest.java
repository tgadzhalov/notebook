package com.example.OnlineNotebook.APITest.AdminController;

import com.example.OnlineNotebook.controller.AdminController;
import com.example.OnlineNotebook.models.dtos.admin.BulkImportUserDto;
import com.example.OnlineNotebook.models.dtos.admin.BulkRegistrationResult;
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

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class CreateBulkUsersApiTest {

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
    private UserService userService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createBulkUsers_whenValidData_thenReturnSuccess() throws Exception {
        List<BulkImportUserDto> users = Arrays.asList(
                BulkImportUserDto.builder()
                        .firstName("Ivan")
                        .lastName("Schweicarov")
                        .email("test1@example.com")
                        .password("12312312")
                        .userType(UserType.STUDENT)
                        .studentClass("10A")
                        .build(),
                BulkImportUserDto.builder()
                        .firstName("Maria")
                        .lastName("Petrova")
                        .email("test2@example.com")
                        .password("12312312")
                        .userType(UserType.STUDENT)
                        .studentClass("10B")
                        .build()
        );

        BulkRegistrationResult result = BulkRegistrationResult.builder()
                .success(true)
                .message("2 users created successfully")
                .build();

        when(userService.registerBulkUsers(any(List.class))).thenReturn(result);

        mockMvc.perform(post("/api/v1/admin/users/import")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(users)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("2 users created successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createBulkUsers_whenPartialFailure_thenReturnFailure() throws Exception {
        List<BulkImportUserDto> users = Arrays.asList(
                BulkImportUserDto.builder()
                        .firstName("Ivan")
                        .lastName("Schweicarov")
                        .email("existing@example.com")
                        .password("12312312")
                        .userType(UserType.STUDENT)
                        .studentClass("10A")
                        .build()
        );

        BulkRegistrationResult result = BulkRegistrationResult.builder()
                .success(false)
                .message("Some users failed to import")
                .build();

        when(userService.registerBulkUsers(any(List.class))).thenReturn(result);

        mockMvc.perform(post("/api/v1/admin/users/import")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(users)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Some users failed to import"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createBulkUsers_whenEmptyList_thenReturnSuccess() throws Exception {
        List<BulkImportUserDto> users = Arrays.asList();

        BulkRegistrationResult result = BulkRegistrationResult.builder()
                .success(true)
                .message("0 users created successfully")
                .build();

        when(userService.registerBulkUsers(any(List.class))).thenReturn(result);

        mockMvc.perform(post("/api/v1/admin/users/import")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(users)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createBulkUsers_whenNotAdmin_thenReturnForbidden() throws Exception {
        List<BulkImportUserDto> users = Arrays.asList(
                BulkImportUserDto.builder()
                        .firstName("Ivan")
                        .lastName("Schweicarov")
                        .email("test@example.com")
                        .password("12312312")
                        .userType(UserType.STUDENT)
                        .build()
        );

        mockMvc.perform(post("/api/v1/admin/users/import")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(users)))
                .andExpect(status().isForbidden());
    }
}

