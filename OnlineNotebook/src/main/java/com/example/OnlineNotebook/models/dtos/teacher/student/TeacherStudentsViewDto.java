package com.example.OnlineNotebook.models.dtos.teacher.student;

import com.example.OnlineNotebook.models.entities.Assignment;
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
public class TeacherStudentsViewDto {
    private List<Course> courses;
    private UUID selectedCourseId;
    private List<User> students;
    private UUID selectedStudentId;
    private User selectedStudent;
    private List<TeacherStudentGradeDto> studentGrades;
    private boolean showGradesModal;
    private String gradeErrorMessage;
    private List<Assignment> assignments;
}
