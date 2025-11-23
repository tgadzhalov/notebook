package com.example.OnlineNotebook.models.dtos.student.home;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StudentLeaderboardEntryDto {

    private final int rank;
    private final String initials;
    private final String fullName;
    private final String className;
    private final String gradeDisplay;
    private final String attendanceDisplay;
    private final boolean currentUser;
}

