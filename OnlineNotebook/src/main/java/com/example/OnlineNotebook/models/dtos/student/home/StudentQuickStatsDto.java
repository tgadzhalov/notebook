package com.example.OnlineNotebook.models.dtos.student.home;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StudentQuickStatsDto {

    private final String averageGradeDisplay;
    private final String attendanceDisplay;
    private final int pendingAssignments;
}

