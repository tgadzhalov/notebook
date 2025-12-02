package com.example.OnlineNotebook.APITest.TeacherController;

import com.example.OnlineNotebook.controller.teacher.GradeController;
import com.example.OnlineNotebook.models.entities.User;
import com.example.OnlineNotebook.models.enums.UserType;
import com.example.OnlineNotebook.security.AuthRoleHandler;
import com.example.OnlineNotebook.security.UserData;
import com.example.OnlineNotebook.services.GradeService;
import com.example.OnlineNotebook.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GradeController.class)
class DeleteGradeApiTest {

    @TestConfiguration
    static class MockConfiguration {
        @Bean
        public UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        public GradeService gradeService() {
            return mock(GradeService.class);
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

    @Autowired
    private GradeService gradeService;

    @Test
    @WithMockUser(username = "teacher@example.com", roles = "TEACHER")
    void deleteGrade_whenValidRequest_thenReturnOk() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID gradeId = UUID.randomUUID();

        UserData userData = UserData.builder()
                .id(teacherId)
                .email("teacher@example.com")
                .userType(UserType.TEACHER)
                .password("password")
                .build();

        User teacher = User.builder()
                .id(teacherId)
                .email("teacher@example.com")
                .userType(UserType.TEACHER)
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(userData, null, userData.getAuthorities());
        when(userService.getById(teacherId)).thenReturn(teacher);
        doNothing().when(gradeService).deleteGradeForTeacher(eq(teacher), eq(gradeId));

        mockMvc.perform(delete("/api/v1/teacher/grades/{gradeId}", gradeId)
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void deleteGrade_whenNotTeacher_thenReturnForbidden() throws Exception {
        UUID gradeId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/teacher/grades/{gradeId}", gradeId)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}

