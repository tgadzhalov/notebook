package com.example.OnlineNotebook.models.dtos.student.grades;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StudentGradeEntryDto {

    private final Integer gradeValue;
    private final String gradeLetter;
    private final String gradeDisplay;
    private final String assignment;
    private final String teacherName;
    private final String gradedOn;
    private final String feedback;
}

