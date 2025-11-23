package com.example.OnlineNotebook.models.dtos.student.home;

import com.example.OnlineNotebook.client.dto.AttendanceResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class StudentHomeViewDto {

    private final StudentProfileDto profile;
    private final StudentQuickStatsDto quickStats;
    private final List<StudentGradePreviewDto> recentGrades;
    private final List<StudentAssignmentPreviewDto> upcomingAssignments;
    private final List<StudentSubjectGradeDto> subjectGrades;
    private final List<StudentLeaderboardEntryDto> leaderboard;
    private final List<AttendanceResponseDto> attendances;
}

