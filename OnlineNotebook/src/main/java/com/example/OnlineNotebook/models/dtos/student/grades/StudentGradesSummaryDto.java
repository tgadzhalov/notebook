package com.example.OnlineNotebook.models.dtos.student.grades;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StudentGradesSummaryDto {

    private final String overallAverageDisplay;
    private final Integer totalGrades;
    private final Integer subjectsCount;
}

