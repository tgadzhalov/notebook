package com.example.OnlineNotebook.services;

import com.example.OnlineNotebook.client.dto.AttendanceRequestDto;
import com.example.OnlineNotebook.client.dto.AttendanceResponseDto;
import com.example.OnlineNotebook.client.AttendanceStatus;
import com.example.OnlineNotebook.client.service.AttendanceClientService;
import com.example.OnlineNotebook.exceptions.ResourceNotFoundException;
import com.example.OnlineNotebook.models.dtos.teacher.attendance.TeacherAttendanceViewDto;
import com.example.OnlineNotebook.models.entities.Course;
import com.example.OnlineNotebook.models.entities.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class AttendanceService {
    private final AttendanceClientService clientAttendanceService;
    private final CourseService courseService;
    private final UserService userService;

    public AttendanceService(AttendanceClientService clientAttendanceService,
                             CourseService courseService,
                             UserService userService) {
        this.clientAttendanceService = clientAttendanceService;
        this.courseService = courseService;
        this.userService = userService;
    }

    public TeacherAttendanceViewDto buildAttendanceView(User teacher, UUID courseId, UUID studentId) {
        List<Course> courses = courseService.getCoursesByTeacher(teacher);

        Course selectedCourse = null;
        if (courseId != null) {
            selectedCourse = courses.stream()
                    .filter(c -> c.getId().equals(courseId))
                    .findFirst()
                    .orElse(null);
        } else if (!courses.isEmpty()) {
            selectedCourse = courses.get(0);
        }

        List<User> students = selectedCourse != null
                ? userService.getStudentsByCourse(selectedCourse)
                : List.of();

        List<AttendanceResponseDto> attendanceRecords = List.of();
        User selectedStudent = null;
        if (studentId != null && selectedCourse != null) {
            selectedStudent = students.stream()
                    .filter(s -> s.getId().equals(studentId))
                    .findFirst()
                    .orElse(null);
            
            if (selectedStudent != null) {
                try {
                    var response = clientAttendanceService.getAttendances(teacher.getId(), studentId);
                    if (response != null && response.getBody() != null) {
                        attendanceRecords = response.getBody();
                    }
                } catch (Exception e) {
                    attendanceRecords = List.of();
                }
            }
        }

        return TeacherAttendanceViewDto.builder()
                .courses(courses)
                .selectedCourse(selectedCourse)
                .students(students)
                .selectedStudentId(studentId)
                .selectedStudent(selectedStudent)
                .attendanceRecords(attendanceRecords)
                .build();
    }

    public void markAttendance(UUID teacherId, UUID studentId, UUID courseId, String status) {
        log.info("Marking attendance - teacherId: {}, studentId: {}, courseId: {}, status: {}", 
            teacherId, studentId, courseId, status);
        User student = userService.getById(studentId);
        Course course = courseService.getCourseById(courseId);
        
        if (course == null) {
            throw new ResourceNotFoundException("Course not found with ID: " + courseId);
        }

        AttendanceRequestDto requestDto = AttendanceRequestDto.builder()
                .studentId(studentId)
                .name(student.getFirstName() + " " + student.getLastName())
                .courseName(course.getName())
                .status(AttendanceStatus.valueOf(status.toUpperCase()))
                .build();

        clientAttendanceService.saveAttendance(teacherId, requestDto);
        log.info("Attendance marked successfully for studentId: {}", studentId);
    }

    public void deleteAttendanceRecord(UUID teacherId, UUID attendanceId) {
        log.info("Deleting attendance record - teacherId: {}, attendanceId: {}", teacherId, attendanceId);
        clientAttendanceService.deleteAttendance(teacherId, attendanceId);
        log.info("Attendance record deleted successfully - attendanceId: {}", attendanceId);
    }
}

