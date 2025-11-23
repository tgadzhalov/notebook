package com.example.OnlineNotebook.models.dtos.student.home;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StudentAssignmentPreviewDto {

    private final String title;
    private final String description;
    private final String subject;
    private final String assignmentType;
    private final String dueDateText;
    private final String priorityLabel;
    private final String priorityLevel;
}

