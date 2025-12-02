package com.example.OnlineNotebook.APITest.UserController;

import com.example.OnlineNotebook.controller.UserController;
import com.example.OnlineNotebook.models.dtos.auth.RegisterDto;
import com.example.OnlineNotebook.security.AuthRoleHandler;
import com.example.OnlineNotebook.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class PostRegisterApiTest {

    @TestConfiguration
    static class MockConfiguration {
        @Bean
        public UserService userService() {
            return mock(UserService.class);
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
    void postRegister_whenValidData_thenRedirectToLogin() throws Exception {
        doNothing().when(userService).registerUser(any(RegisterDto.class));

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("firstName", "Ivan")
                        .param("lastName", "Schweicarov")
                        .param("email", "test@example.com")
                        .param("password", "12312312")
                        .param("userType", "STUDENT"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void postRegister_whenEmailExists_thenReturnError() throws Exception {
        doThrow(new IllegalArgumentException("Email already exists: existing@example.com"))
                .when(userService).registerUser(any(RegisterDto.class));

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("firstName", "Ivan")
                        .param("lastName", "Schweicarov")
                        .param("email", "existing@example.com")
                        .param("password", "12312312")
                        .param("userType", "STUDENT"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void postRegister_whenInvalidData_thenReturnRegisterView() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("firstName", "")  // Invalid: empty first name
                        .param("lastName", "Schweicarov")
                        .param("email", "invalid-email")  // Invalid: not a valid email
                        .param("password", "123")  // Invalid: too short
                        .param("userType", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    void postRegister_whenMissingRequiredFields_thenReturnRegisterView() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("firstName", "Ivan")
                        .param("lastName", "Schweicarov")
                        // Missing email and password
                        .param("userType", "STUDENT"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }
}

