package com.example.OnlineNotebook.models.dtos.teacher.assignment;

import com.example.OnlineNotebook.models.enums.AssignmentType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class AssignmentFormDto {

    @NotNull
    private UUID courseId;

    @NotBlank
    @Size(min = 2, max = 100)
    private String title;

    @Size(max = 500)
    private String description;

    @NotNull
    private AssignmentType assignmentType;

    @NotNull
    @FutureOrPresent
    private LocalDate dueDate;
}
