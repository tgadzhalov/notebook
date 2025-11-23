package com.example.OnlineNotebook.models.dtos.teacher.assignment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentSummaryDto {
    private UUID id;
    private String title;
    private String courseName;
    private String assignmentType;
    private LocalDate dueDate;
}
