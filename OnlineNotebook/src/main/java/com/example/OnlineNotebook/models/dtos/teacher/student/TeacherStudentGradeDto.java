package com.example.OnlineNotebook.models.dtos.teacher.student;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class TeacherStudentGradeDto {
    private UUID gradeId;
    private String subjectCode;
    private String subject;
    private String gradeTypeCode;
    private String gradeType;
    private String gradeLetter;
    private Integer gradeValue;
    private String gradeDisplay;
    private LocalDate gradedOn;
    private String gradedBy;
    private String feedback;
}


