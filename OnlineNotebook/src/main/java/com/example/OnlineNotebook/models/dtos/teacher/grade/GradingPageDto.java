package com.example.OnlineNotebook.models.dtos.teacher.grade;

import com.example.OnlineNotebook.models.dtos.teacher.grade.GradeTypeOptionDto;
import com.example.OnlineNotebook.models.entities.Course;
import com.example.OnlineNotebook.models.entities.Grade;
import com.example.OnlineNotebook.models.entities.User;
import com.example.OnlineNotebook.models.enums.SubjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GradingPageDto {
    private User user;
    private List<Course> courses;
    private Course selectedCourse;
    private UUID selectedCourseId;
    private List<SubjectType> subjects;
    private String selectedSubject;
    private List<GradeTypeOptionDto> assignments;
    private UUID selectedAssignmentId;
    private List<User> students;
    private Map<UUID, Grade> studentGrades;
}
