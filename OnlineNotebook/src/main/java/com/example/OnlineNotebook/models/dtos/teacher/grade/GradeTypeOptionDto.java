package com.example.OnlineNotebook.models.dtos.teacher.grade;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Data
@NoArgsConstructor
public class GradeTypeOptionDto {
    private UUID id;
    private String name;
    private String title;

    @Builder
    public GradeTypeOptionDto(String name, String title) {
        this.name = name;
        this.title = title;
        this.id = generateStableId(name);
    }

    public void setName(String name) {
        this.name = name;
        this.id = name != null ? generateStableId(name) : null;
    }

    private static UUID generateStableId(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Grade type name cannot be null");
        }
        return UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));
    }
}
