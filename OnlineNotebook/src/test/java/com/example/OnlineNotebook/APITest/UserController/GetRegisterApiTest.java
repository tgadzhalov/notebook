package com.example.OnlineNotebook.APITest.UserController;

import com.example.OnlineNotebook.controller.UserController;
import com.example.OnlineNotebook.security.AuthRoleHandler;
import com.example.OnlineNotebook.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class GetRegisterApiTest {

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

    @Test
    void getRegister_whenRequested_thenReturnRegisterView() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerDto"));
    }
}




