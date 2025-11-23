package com.example.OnlineNotebook.models.dtos.student.grades;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class StudentSubjectGradesDto {

    private final String subjectName;
    private final String subjectCode;
    private final String averageDisplay;
    private final List<StudentGradeEntryDto> grades;
}

