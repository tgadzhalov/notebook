package com.example.OnlineNotebook.APITest.AdminController;

import com.example.OnlineNotebook.controller.AdminController;
import com.example.OnlineNotebook.models.dtos.admin.UserRegistrationResult;
import com.example.OnlineNotebook.models.dtos.auth.RegisterDto;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class CreateUserApiTest {

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

    @Autowired
    private CourseService courseService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_whenValidData_thenReturnSuccess() throws Exception {
        RegisterDto registerDto = RegisterDto.builder()
                .firstName("Ivan")
                .lastName("Schweicarov")
                .email("test@example.com")
                .password("12312312")
                .userType(UserType.STUDENT)
                .build();

        UserRegistrationResult result = new UserRegistrationResult();
        result.setSuccess(true);
        result.setMessage("User created successfully");

        when(userService.registerUserWithResult(any(RegisterDto.class))).thenReturn(result);

        mockMvc.perform(post("/api/v1/admin/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User created successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_whenEmailExists_thenReturnFailure() throws Exception {
        RegisterDto registerDto = RegisterDto.builder()
                .firstName("Ivan")
                .lastName("Schweicarov")
                .email("existing@example.com")
                .password("12312312")
                .userType(UserType.STUDENT)
                .build();

        UserRegistrationResult result = new UserRegistrationResult();
        result.setSuccess(false);
        result.setMessage("Email already exists: existing@example.com");

        when(userService.registerUserWithResult(any(RegisterDto.class))).thenReturn(result);

        mockMvc.perform(post("/api/v1/admin/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email already exists: existing@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_whenInvalidData_thenReturnBadRequest() throws Exception {
        RegisterDto registerDto = RegisterDto.builder()
                .firstName("")
                .lastName("Schweicarov")
                .email("invalid-email")
                .password("123")
                .userType(UserType.STUDENT)
                .build();

        mockMvc.perform(post("/api/v1/admin/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createUser_whenNotAdmin_thenReturnForbidden() throws Exception {
        RegisterDto registerDto = RegisterDto.builder()
                .firstName("Ivan")
                .lastName("Schweicarov")
                .email("test@example.com")
                .password("12312312")
                .userType(UserType.STUDENT)
                .build();

        mockMvc.perform(post("/api/v1/admin/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto)))
                .andExpect(status().isForbidden());
    }
}
