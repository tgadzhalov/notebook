package com.example.OnlineNotebook.models.dtos.student.grades;

import com.example.OnlineNotebook.models.dtos.student.home.StudentProfileDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class StudentGradesViewDto {

    private final StudentProfileDto profile;
    private final StudentGradesSummaryDto summary;
    private final List<StudentSubjectGradesDto> subjects;
}

