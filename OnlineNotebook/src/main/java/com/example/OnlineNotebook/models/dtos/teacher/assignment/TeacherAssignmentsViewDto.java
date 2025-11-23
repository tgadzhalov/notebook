package com.example.OnlineNotebook.models.dtos.teacher.assignment;

import com.example.OnlineNotebook.models.entities.Course;
import com.example.OnlineNotebook.models.enums.AssignmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAssignmentsViewDto {
    private AssignmentFormDto form;
    private List<Course> courses;
    private List<AssignmentType> assignmentTypes;
}
