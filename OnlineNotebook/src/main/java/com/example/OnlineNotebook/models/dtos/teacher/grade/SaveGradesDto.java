package com.example.OnlineNotebook.models.dtos.teacher.grade;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveGradesDto {
    @NotNull
    private UUID courseId;

    @NotNull
    private UUID assignmentId;

    @NotNull
    private String subjectType;

    @NotNull
    private LocalDate gradeDate;

    private Map<String, String> studentGrades;

    public Map<UUID, String> getStudentGradesAsUuidMap() {
        if (studentGrades == null) {
            return Map.of();
        }
        return studentGrades.entrySet().stream()
                .collect(Collectors.toMap(entry -> UUID.fromString(entry.getKey()), Map.Entry::getValue));
    }
}
