package com.example.OnlineNotebook.models.dtos.teacher.home;

import com.example.OnlineNotebook.models.dtos.teacher.assignment.AssignmentSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherHomeViewDto {
    private List<AssignmentSummaryDto> assignments;
}
