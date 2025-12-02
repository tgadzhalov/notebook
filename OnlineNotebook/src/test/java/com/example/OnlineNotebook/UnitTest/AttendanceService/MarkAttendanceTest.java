package com.example.OnlineNotebook.UnitTest.AttendanceService;

import com.example.OnlineNotebook.client.dto.AttendanceRequestDto;
import com.example.OnlineNotebook.client.AttendanceStatus;
import com.example.OnlineNotebook.client.service.AttendanceClientService;
import com.example.OnlineNotebook.exceptions.ResourceNotFoundException;
import com.example.OnlineNotebook.models.entities.Course;
import com.example.OnlineNotebook.models.entities.User;
import com.example.OnlineNotebook.models.enums.UserType;
import com.example.OnlineNotebook.services.AttendanceService;
import com.example.OnlineNotebook.services.CourseService;
import com.example.OnlineNotebook.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MarkAttendanceTest {

    @InjectMocks
    private AttendanceService attendanceService;
    @Mock
    private AttendanceClientService clientAttendanceService;
    @Mock
    private CourseService courseService;
    @Mock
    private UserService userService;

    @Test
    void markAttendance_whenCourseNotFound_thenThrowResourceNotFoundException() {
        UUID teacherId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        User student = User.builder()
                .id(studentId)
                .firstName("Alice")
                .lastName("Smith")
                .userType(UserType.STUDENT)
                .build();

        when(userService.getById(studentId)).thenReturn(student);
        when(courseService.getCourseById(courseId)).thenReturn(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> attendanceService.markAttendance(teacherId, studentId, courseId, "ABSENT"));

        assertEquals("Course not found with ID: " + courseId, exception.getMessage());
        verify(userService, times(1)).getById(studentId);
        verify(courseService, times(1)).getCourseById(courseId);
        verify(clientAttendanceService, never()).saveAttendance(any(), any());
    }

    @Test
    void markAttendance_whenStatusIsAbsent_thenShouldSaveAttendance() {
        UUID teacherId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        User student = User.builder()
                .id(studentId)
                .firstName("Alice")
                .lastName("Smith")
                .userType(UserType.STUDENT)
                .build();

        Course course = Course.builder()
                .id(courseId)
                .name("Mathematics")
                .build();

        when(userService.getById(studentId)).thenReturn(student);
        when(courseService.getCourseById(courseId)).thenReturn(course);
        when(clientAttendanceService.saveAttendance(any(), any())).thenReturn(ResponseEntity.ok().build());

        assertDoesNotThrow(() -> attendanceService.markAttendance(teacherId, studentId, courseId, "ABSENT"));

        ArgumentCaptor<AttendanceRequestDto> dtoCaptor = ArgumentCaptor.forClass(AttendanceRequestDto.class);
        verify(userService, times(1)).getById(studentId);
        verify(courseService, times(1)).getCourseById(courseId);
        verify(clientAttendanceService, times(1)).saveAttendance(eq(teacherId), dtoCaptor.capture());
        
        AttendanceRequestDto capturedDto = dtoCaptor.getValue();
        assertEquals(studentId, capturedDto.getStudentId());
        assertEquals("Alice Smith", capturedDto.getName());
        assertEquals("Mathematics", capturedDto.getCourseName());
        assertEquals(AttendanceStatus.ABSENT, capturedDto.getStatus());
    }

    @Test
    void markAttendance_whenStatusIsLate_thenShouldSaveAttendance() {
        UUID teacherId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        User student = User.builder()
                .id(studentId)
                .firstName("Bob")
                .lastName("Jones")
                .userType(UserType.STUDENT)
                .build();

        Course course = Course.builder()
                .id(courseId)
                .name("Physics")
                .build();

        when(userService.getById(studentId)).thenReturn(student);
        when(courseService.getCourseById(courseId)).thenReturn(course);
        when(clientAttendanceService.saveAttendance(any(), any())).thenReturn(ResponseEntity.ok().build());

        assertDoesNotThrow(() -> attendanceService.markAttendance(teacherId, studentId, courseId, "LATE"));

        ArgumentCaptor<AttendanceRequestDto> dtoCaptor = ArgumentCaptor.forClass(AttendanceRequestDto.class);
        verify(userService, times(1)).getById(studentId);
        verify(courseService, times(1)).getCourseById(courseId);
        verify(clientAttendanceService, times(1)).saveAttendance(eq(teacherId), dtoCaptor.capture());
        
        AttendanceRequestDto capturedDto = dtoCaptor.getValue();
        assertEquals(studentId, capturedDto.getStudentId());
        assertEquals("Bob Jones", capturedDto.getName());
        assertEquals("Physics", capturedDto.getCourseName());
        assertEquals(AttendanceStatus.LATE, capturedDto.getStatus());
    }

    @Test
    void markAttendance_whenStatusIsLowerCase_thenShouldConvertToUpperCase() {
        UUID teacherId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        User student = User.builder()
                .id(studentId)
                .firstName("Charlie")
                .lastName("Brown")
                .userType(UserType.STUDENT)
                .build();

        Course course = Course.builder()
                .id(courseId)
                .name("Chemistry")
                .build();

        when(userService.getById(studentId)).thenReturn(student);
        when(courseService.getCourseById(courseId)).thenReturn(course);
        when(clientAttendanceService.saveAttendance(any(), any())).thenReturn(ResponseEntity.ok().build());

        assertDoesNotThrow(() -> attendanceService.markAttendance(teacherId, studentId, courseId, "absent"));

        ArgumentCaptor<AttendanceRequestDto> dtoCaptor = ArgumentCaptor.forClass(AttendanceRequestDto.class);
        verify(clientAttendanceService, times(1)).saveAttendance(eq(teacherId), dtoCaptor.capture());
        
        AttendanceRequestDto capturedDto = dtoCaptor.getValue();
        assertEquals(AttendanceStatus.ABSENT, capturedDto.getStatus());
    }
}












