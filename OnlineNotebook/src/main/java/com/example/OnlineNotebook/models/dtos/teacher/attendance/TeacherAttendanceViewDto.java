package com.example.OnlineNotebook.models.dtos.teacher.attendance;

import com.example.OnlineNotebook.client.dto.AttendanceResponseDto;
import com.example.OnlineNotebook.models.entities.Course;
import com.example.OnlineNotebook.models.entities.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAttendanceViewDto {
    private List<Course> courses;
    private Course selectedCourse;
    private List<User> students;
    private UUID selectedStudentId;
    private User selectedStudent;
    private List<AttendanceResponseDto> attendanceRecords;
}

