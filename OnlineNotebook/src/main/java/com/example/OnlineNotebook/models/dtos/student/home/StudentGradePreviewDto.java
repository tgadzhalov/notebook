package com.example.OnlineNotebook.models.dtos.student.home;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StudentGradePreviewDto {

    private final String subject;
    private final String assignment;
    private final Integer gradeValue;
    private final String gradeLetter;
}

