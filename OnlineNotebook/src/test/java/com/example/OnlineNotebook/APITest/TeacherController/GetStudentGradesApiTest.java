package com.example.OnlineNotebook.APITest.TeacherController;

import com.example.OnlineNotebook.controller.teacher.GradeController;
import com.example.OnlineNotebook.models.dtos.teacher.student.TeacherStudentGradeDto;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GradeController.class)
class GetStudentGradesApiTest {

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
    void getStudentGrades_whenValidRequest_thenReturnGrades() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

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

        List<TeacherStudentGradeDto> grades = new ArrayList<>();
        TeacherStudentGradeDto grade1 = TeacherStudentGradeDto.builder()
                .gradeId(UUID.randomUUID())
                .subjectCode("MATH")
                .subject("Mathematics")
                .gradeTypeCode("EXAM")
                .gradeType("Exam")
                .gradeLetter("A")
                .gradeValue(95)
                .gradeDisplay("A (95)")
                .gradedOn(LocalDate.now())
                .gradedBy("John Doe")
                .feedback("Excellent work")
                .build();
        grades.add(grade1);

        Authentication auth = new UsernamePasswordAuthenticationToken(userData, null, userData.getAuthorities());
        when(userService.getById(teacherId)).thenReturn(teacher);
        when(gradeService.getStudentGradesForTeacher(eq(teacher), eq(studentId), any(UUID.class)))
                .thenReturn(grades);

        mockMvc.perform(get("/api/v1/teacher/students/{studentId}/grades", studentId)
                        .with(authentication(auth))
                        .with(csrf())
                        .param("courseId", courseId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].subject").value("Mathematics"))
                .andExpect(jsonPath("$[0].gradeLetter").value("A"))
                .andExpect(jsonPath("$[0].gradeValue").value(95));
    }

    @Test
    @WithMockUser(username = "teacher@example.com", roles = "TEACHER")
    void getStudentGrades_whenNoCourseId_thenReturnGrades() throws Exception {
        UUID teacherId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();

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

        List<TeacherStudentGradeDto> grades = new ArrayList<>();
        Authentication auth = new UsernamePasswordAuthenticationToken(userData, null, userData.getAuthorities());
        when(userService.getById(teacherId)).thenReturn(teacher);
        when(gradeService.getStudentGradesForTeacher(eq(teacher), eq(studentId), eq(null)))
                .thenReturn(grades);

        mockMvc.perform(get("/api/v1/teacher/students/{studentId}/grades", studentId)
                        .with(authentication(auth))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void getStudentGrades_whenNotTeacher_thenReturnForbidden() throws Exception {
        UUID studentId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/teacher/students/{studentId}/grades", studentId)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }
}

