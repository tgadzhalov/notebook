package com.example.OnlineNotebook.models.dtos.teacher.grade;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGradeFeedbackDto {
    private UUID studentId;
    private UUID courseId;

    @Size(max = 500, message = "Feedback must be 500 characters or fewer")
    private String feedback;
}


