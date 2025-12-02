package com.example.OnlineNotebook.UnitTest.AttendanceService;

import com.example.OnlineNotebook.client.dto.AttendanceResponseDto;
import com.example.OnlineNotebook.client.AttendanceStatus;
import com.example.OnlineNotebook.client.service.AttendanceClientService;
import com.example.OnlineNotebook.models.dtos.teacher.attendance.TeacherAttendanceViewDto;
import com.example.OnlineNotebook.models.entities.Course;
import com.example.OnlineNotebook.models.entities.User;
import com.example.OnlineNotebook.models.enums.UserType;
import com.example.OnlineNotebook.services.AttendanceService;
import com.example.OnlineNotebook.services.CourseService;
import com.example.OnlineNotebook.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BuildAttendanceViewTest {

    @InjectMocks
    private AttendanceService attendanceService;
    @Mock
    private AttendanceClientService clientAttendanceService;
    @Mock
    private CourseService courseService;
    @Mock
    private UserService userService;

    @Test
    void buildAttendanceView_whenCourseIdIsNullAndCoursesIsEmpty_thenShouldReturnDtoWithNoSelectedCourse() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Ivanov")
                .email("email@example.com")
                .userType(UserType.TEACHER)
                .build();

        when(courseService.getCoursesByTeacher(teacher)).thenReturn(Collections.emptyList());

        TeacherAttendanceViewDto result = attendanceService.buildAttendanceView(teacher, null, null);

        assertNotNull(result);
        assertTrue(result.getCourses().isEmpty());
        assertNull(result.getSelectedCourse());
        assertTrue(result.getStudents().isEmpty());
        assertNull(result.getSelectedStudentId());
        assertNull(result.getSelectedStudent());
        assertTrue(result.getAttendanceRecords().isEmpty());
        verify(courseService, times(1)).getCoursesByTeacher(teacher);
        verify(userService, never()).getStudentsByCourse(any());
        verify(clientAttendanceService, never()).getAttendances(any(), any());
    }

    @Test
    void buildAttendanceView_whenCourseIdIsNullAndCoursesIsNotEmpty_thenShouldSelectFirstCourse() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Ivanov")
                .email("email@example.com")
                .userType(UserType.TEACHER)
                .build();

        Course course1 = Course.builder()
                .id(UUID.randomUUID())
                .name("Mathematics")
                .teacher(teacher)
                .build();

        Course course2 = Course.builder()
                .id(UUID.randomUUID())
                .name("Physics")
                .teacher(teacher)
                .build();

        List<Course> courses = List.of(course1, course2);
        when(courseService.getCoursesByTeacher(teacher)).thenReturn(courses);
        when(userService.getStudentsByCourse(course1)).thenReturn(Collections.emptyList());

        TeacherAttendanceViewDto result = attendanceService.buildAttendanceView(teacher, null, null);

        assertNotNull(result);
        assertEquals(2, result.getCourses().size());
        assertEquals(course1, result.getSelectedCourse());
        assertTrue(result.getStudents().isEmpty());
        assertNull(result.getSelectedStudentId());
        assertNull(result.getSelectedStudent());
        assertTrue(result.getAttendanceRecords().isEmpty());
        verify(courseService, times(1)).getCoursesByTeacher(teacher);
        verify(userService, times(1)).getStudentsByCourse(course1);
        verify(clientAttendanceService, never()).getAttendances(any(), any());
    }

    @Test
    void buildAttendanceView_whenCourseIdIsNotNullAndCourseFound_thenShouldReturnDtoWithSelectedCourse() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Ivanov")
                .email("email@example.com")
                .userType(UserType.TEACHER)
                .build();

        Course course1 = Course.builder()
                .id(UUID.randomUUID())
                .name("Mathematics")
                .teacher(teacher)
                .build();

        Course course2 = Course.builder()
                .id(UUID.randomUUID())
                .name("Physics")
                .teacher(teacher)
                .build();

        List<Course> courses = List.of(course1, course2);
        when(courseService.getCoursesByTeacher(teacher)).thenReturn(courses);
        when(userService.getStudentsByCourse(course2)).thenReturn(Collections.emptyList());

        TeacherAttendanceViewDto result = attendanceService.buildAttendanceView(teacher, course2.getId(), null);

        assertNotNull(result);
        assertEquals(2, result.getCourses().size());
        assertEquals(course2, result.getSelectedCourse());
        assertTrue(result.getStudents().isEmpty());
        assertNull(result.getSelectedStudentId());
        assertNull(result.getSelectedStudent());
        assertTrue(result.getAttendanceRecords().isEmpty());
        verify(courseService, times(1)).getCoursesByTeacher(teacher);
        verify(userService, times(1)).getStudentsByCourse(course2);
        verify(clientAttendanceService, never()).getAttendances(any(), any());
    }

    @Test
    void buildAttendanceView_whenCourseIdIsNotNullAndCourseNotFound_thenShouldReturnDtoWithNullSelectedCourse() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Ivanov")
                .email("email@example.com")
                .userType(UserType.TEACHER)
                .build();

        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("Mathematics")
                .teacher(teacher)
                .build();

        UUID nonExistentCourseId = UUID.randomUUID();
        List<Course> courses = List.of(course);
        when(courseService.getCoursesByTeacher(teacher)).thenReturn(courses);

        TeacherAttendanceViewDto result = attendanceService.buildAttendanceView(teacher, nonExistentCourseId, null);

        assertNotNull(result);
        assertEquals(1, result.getCourses().size());
        assertNull(result.getSelectedCourse());
        assertTrue(result.getStudents().isEmpty());
        assertNull(result.getSelectedStudentId());
        assertNull(result.getSelectedStudent());
        assertTrue(result.getAttendanceRecords().isEmpty());
        verify(courseService, times(1)).getCoursesByTeacher(teacher);
        verify(userService, never()).getStudentsByCourse(any());
        verify(clientAttendanceService, never()).getAttendances(any(), any());
    }

    @Test
    void buildAttendanceView_whenStudentIdIsNotNullAndStudentFound_thenShouldReturnDtoWithAttendanceRecords() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Ivanov")
                .email("email@example.com")
                .userType(UserType.TEACHER)
                .build();

        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("Mathematics")
                .teacher(teacher)
                .build();

        User student = User.builder()
                .id(UUID.randomUUID())
                .firstName("Dragan")
                .lastName("Smetanov")
                .email("email2@example.com")
                .userType(UserType.STUDENT)
                .course(course)
                .build();

        AttendanceResponseDto attendanceRecord = AttendanceResponseDto.builder()
                .id(UUID.randomUUID())
                .studentId(student.getId())
                .status(AttendanceStatus.ABSENT)
                .markedById(teacher.getId())
                .studentName("Dragan Smetanov")
                .studentCourse("Mathematics")
                .createdAt(LocalDateTime.now())
                .build();

        List<Course> courses = List.of(course);
        List<User> students = List.of(student);
        List<AttendanceResponseDto> attendanceRecords = List.of(attendanceRecord);

        when(courseService.getCoursesByTeacher(teacher)).thenReturn(courses);
        when(userService.getStudentsByCourse(course)).thenReturn(students);
        when(clientAttendanceService.getAttendances(teacher.getId(), student.getId()))
                .thenReturn(ResponseEntity.ok(attendanceRecords));

        TeacherAttendanceViewDto result = attendanceService.buildAttendanceView(teacher, course.getId(), student.getId());

        assertNotNull(result);
        assertEquals(course, result.getSelectedCourse());
        assertEquals(1, result.getStudents().size());
        assertEquals(student.getId(), result.getSelectedStudentId());
        assertEquals(student, result.getSelectedStudent());
        assertEquals(1, result.getAttendanceRecords().size());
        assertEquals(attendanceRecord, result.getAttendanceRecords().get(0));
        verify(courseService, times(1)).getCoursesByTeacher(teacher);
        verify(userService, times(1)).getStudentsByCourse(course);
        verify(clientAttendanceService, times(1)).getAttendances(teacher.getId(), student.getId());
    }

    @Test
    void buildAttendanceView_whenStudentIdIsNotNullAndStudentNotFound_thenShouldReturnDtoWithNullSelectedStudent() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("Ivan")
                .lastName("Ivanov")
                .email("email@example.com")
                .userType(UserType.TEACHER)
                .build();

        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("Mathematics")
                .teacher(teacher)
                .build();

        User student = User.builder()
                .id(UUID.randomUUID())
                .firstName("Dragan")
                .lastName("Smetanov")
                .email("email2@example.com")
                .userType(UserType.STUDENT)
                .course(course)
                .build();

        UUID nonExistentStudentId = UUID.randomUUID();
        List<Course> courses = List.of(course);
        List<User> students = List.of(student);

        when(courseService.getCoursesByTeacher(teacher)).thenReturn(courses);
        when(userService.getStudentsByCourse(course)).thenReturn(students);

        TeacherAttendanceViewDto result = attendanceService.buildAttendanceView(teacher, course.getId(), nonExistentStudentId);

        assertNotNull(result);
        assertEquals(course, result.getSelectedCourse());
        assertEquals(1, result.getStudents().size());
        assertEquals(nonExistentStudentId, result.getSelectedStudentId());
        assertNull(result.getSelectedStudent());
        assertTrue(result.getAttendanceRecords().isEmpty());
        verify(courseService, times(1)).getCoursesByTeacher(teacher);
        verify(userService, times(1)).getStudentsByCourse(course);
        verify(clientAttendanceService, never()).getAttendances(any(), any());
    }

    @Test
    void buildAttendanceView_whenClientServiceThrowsException_thenShouldReturnEmptyAttendanceRecords() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .userType(UserType.TEACHER)
                .build();

        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("Mathematics")
                .teacher(teacher)
                .build();

        User student = User.builder()
                .id(UUID.randomUUID())
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .userType(UserType.STUDENT)
                .course(course)
                .build();

        List<Course> courses = List.of(course);
        List<User> students = List.of(student);

        when(courseService.getCoursesByTeacher(teacher)).thenReturn(courses);
        when(userService.getStudentsByCourse(course)).thenReturn(students);
        when(clientAttendanceService.getAttendances(teacher.getId(), student.getId()))
                .thenThrow(new RuntimeException("Service unavailable"));

        TeacherAttendanceViewDto result = attendanceService.buildAttendanceView(teacher, course.getId(), student.getId());

        assertNotNull(result);
        assertEquals(course, result.getSelectedCourse());
        assertEquals(student, result.getSelectedStudent());
        assertTrue(result.getAttendanceRecords().isEmpty());
        verify(courseService, times(1)).getCoursesByTeacher(teacher);
        verify(userService, times(1)).getStudentsByCourse(course);
        verify(clientAttendanceService, times(1)).getAttendances(teacher.getId(), student.getId());
    }

    @Test
    void buildAttendanceView_whenClientServiceReturnsNullResponse_thenShouldReturnEmptyAttendanceRecords() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .userType(UserType.TEACHER)
                .build();

        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("Mathematics")
                .teacher(teacher)
                .build();

        User student = User.builder()
                .id(UUID.randomUUID())
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .userType(UserType.STUDENT)
                .course(course)
                .build();

        List<Course> courses = List.of(course);
        List<User> students = List.of(student);

        when(courseService.getCoursesByTeacher(teacher)).thenReturn(courses);
        when(userService.getStudentsByCourse(course)).thenReturn(students);
        when(clientAttendanceService.getAttendances(teacher.getId(), student.getId()))
                .thenReturn(null);

        TeacherAttendanceViewDto result = attendanceService.buildAttendanceView(teacher, course.getId(), student.getId());

        assertNotNull(result);
        assertEquals(course, result.getSelectedCourse());
        assertEquals(student, result.getSelectedStudent());
        assertTrue(result.getAttendanceRecords().isEmpty());
        verify(courseService, times(1)).getCoursesByTeacher(teacher);
        verify(userService, times(1)).getStudentsByCourse(course);
        verify(clientAttendanceService, times(1)).getAttendances(teacher.getId(), student.getId());
    }

    @Test
    void buildAttendanceView_whenClientServiceReturnsNullBody_thenShouldReturnEmptyAttendanceRecords() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .userType(UserType.TEACHER)
                .build();

        Course course = Course.builder()
                .id(UUID.randomUUID())
                .name("Mathematics")
                .teacher(teacher)
                .build();

        User student = User.builder()
                .id(UUID.randomUUID())
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .userType(UserType.STUDENT)
                .course(course)
                .build();

        List<Course> courses = List.of(course);
        List<User> students = List.of(student);
        ResponseEntity<List<AttendanceResponseDto>> response = ResponseEntity.ok(null);

        when(courseService.getCoursesByTeacher(teacher)).thenReturn(courses);
        when(userService.getStudentsByCourse(course)).thenReturn(students);
        when(clientAttendanceService.getAttendances(teacher.getId(), student.getId()))
                .thenReturn(response);

        TeacherAttendanceViewDto result = attendanceService.buildAttendanceView(teacher, course.getId(), student.getId());

        assertNotNull(result);
        assertEquals(course, result.getSelectedCourse());
        assertEquals(student, result.getSelectedStudent());
        assertTrue(result.getAttendanceRecords().isEmpty());
        verify(courseService, times(1)).getCoursesByTeacher(teacher);
        verify(userService, times(1)).getStudentsByCourse(course);
        verify(clientAttendanceService, times(1)).getAttendances(teacher.getId(), student.getId());
    }

    @Test
    void buildAttendanceView_whenStudentIdIsNotNullButCourseIsNull_thenShouldNotCallClientService() {
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .userType(UserType.TEACHER)
                .build();

        UUID studentId = UUID.randomUUID();
        when(courseService.getCoursesByTeacher(teacher)).thenReturn(Collections.emptyList());

        TeacherAttendanceViewDto result = attendanceService.buildAttendanceView(teacher, null, studentId);

        assertNotNull(result);
        assertNull(result.getSelectedCourse());
        assertTrue(result.getStudents().isEmpty());
        assertEquals(studentId, result.getSelectedStudentId());
        assertNull(result.getSelectedStudent());
        assertTrue(result.getAttendanceRecords().isEmpty());
        verify(courseService, times(1)).getCoursesByTeacher(teacher);
        verify(userService, never()).getStudentsByCourse(any());
        verify(clientAttendanceService, never()).getAttendances(any(), any());
    }
}
