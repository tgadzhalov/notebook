package com.example.OnlineNotebook.models.dtos.student.home;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StudentSubjectGradeDto {

    private final String subject;
    private final String subjectCode;
    private final String gradeDisplay;
    private final int percentage;
}

