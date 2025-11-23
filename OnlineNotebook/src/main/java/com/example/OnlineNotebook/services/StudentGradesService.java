package com.example.OnlineNotebook.services;

import com.example.OnlineNotebook.models.dtos.student.grades.StudentGradeEntryDto;
import com.example.OnlineNotebook.models.dtos.student.grades.StudentGradesSummaryDto;
import com.example.OnlineNotebook.models.dtos.student.grades.StudentGradesViewDto;
import com.example.OnlineNotebook.models.dtos.student.grades.StudentSubjectGradesDto;
import com.example.OnlineNotebook.models.dtos.student.home.StudentProfileDto;
import com.example.OnlineNotebook.models.entities.Grade;
import com.example.OnlineNotebook.models.entities.User;
import com.example.OnlineNotebook.models.enums.GradeLetter;
import com.example.OnlineNotebook.models.enums.SubjectType;
import com.example.OnlineNotebook.repositories.GradeRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StudentGradesService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

    private final UserService userService;
    private final GradeRepository gradeRepository;

    public StudentGradesService(UserService userService,
                                GradeRepository gradeRepository) {
        this.userService = userService;
        this.gradeRepository = gradeRepository;
    }

    public StudentGradesViewDto buildGradesView(UUID studentId) {
        User student = userService.getById(studentId);
        List<Grade> grades = gradeRepository.findByStudent(student);

        StudentGradesSummaryDto summary = buildSummary(grades);
        List<StudentSubjectGradesDto> subjects = buildSubjectSections(grades);

        return StudentGradesViewDto.builder()
                .profile(buildProfile(student))
                .summary(summary)
                .subjects(subjects)
                .build();
    }

    private StudentProfileDto buildProfile(User student) {
        return StudentProfileDto.builder()
                .id(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .studentClass(student.getStudentClass())
                .courseName(student.getCourse() != null ? student.getCourse().getName() : null)
                .profilePictureUrl(student.getProfilePictureUrl())
                .initials(buildInitials(student.getFirstName(), student.getLastName()))
                .displayId(student.getId() != null ? "ID: " + student.getId() : "ID: --")
                .build();
    }

    private StudentGradesSummaryDto buildSummary(List<Grade> grades) {
        double overallAverage = grades.stream()
                .map(Grade::getGradeLetter)
                .map(this::resolveGradeValue)
                .filter(value -> value > 0)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(Double.NaN);

        long subjectsCount = grades.stream()
                .map(Grade::getSubjectType)
                .filter(subject -> subject != null)
                .distinct()
                .count();

        return StudentGradesSummaryDto.builder()
                .overallAverageDisplay(Double.isNaN(overallAverage) ? "--" : formatAverage(overallAverage))
                .totalGrades(grades.size())
                .subjectsCount((int) subjectsCount)
                .build();
    }

    private List<StudentSubjectGradesDto> buildSubjectSections(List<Grade> grades) {
        Map<SubjectType, List<Grade>> grouped = grades.stream()
                .filter(grade -> grade.getSubjectType() != null)
                .collect(Collectors.groupingBy(Grade::getSubjectType));

        return grouped.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().getDisplayType()))
                .map(entry -> StudentSubjectGradesDto.builder()
                        .subjectName(entry.getKey().getDisplayType())
                        .subjectCode(entry.getKey().name())
                        .averageDisplay(buildSubjectAverage(entry.getValue()))
                        .grades(buildGradeEntries(entry.getValue()))
                        .build())
                .collect(Collectors.toList());
    }

    private String buildSubjectAverage(List<Grade> subjectGrades) {
        double subjectAverage = subjectGrades.stream()
                .map(Grade::getGradeLetter)
                .map(this::resolveGradeValue)
                .filter(value -> value > 0)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(Double.NaN);

        if (Double.isNaN(subjectAverage)) {
            return "--";
        }

        int percentage = (int) Math.round((subjectAverage / 6.0) * 100);
        return formatAverage(subjectAverage) + " (" + percentage + "%)";
    }

    private List<StudentGradeEntryDto> buildGradeEntries(List<Grade> grades) {
        return grades.stream()
                .sorted(Comparator.comparing(Grade::getDateGraded).reversed())
                .map(grade -> StudentGradeEntryDto.builder()
                        .gradeValue(resolveGradeValue(grade.getGradeLetter()))
                        .gradeLetter(grade.getGradeLetter() != null ? grade.getGradeLetter().name() : null)
                        .gradeDisplay(grade.getGradeLetter() != null ? grade.getGradeLetter().getDisplayType() : "--")
                        .assignment(grade.getGradeType() != null ? grade.getGradeType().getDisplayType() : "Assessment")
                        .teacherName(buildTeacherName(grade.getGradedBy()))
                        .gradedOn(grade.getDateGraded() != null ? grade.getDateGraded().toLocalDate().format(DATE_FORMATTER) : "--")
                        .feedback(grade.getFeedback())
                        .build())
                .collect(Collectors.toList());
    }

    private int resolveGradeValue(GradeLetter gradeLetter) {
        if (gradeLetter == null) {
            return 0;
        }
        return switch (gradeLetter) {
            case BAD -> 2;
            case AVERAGE -> 3;
            case GOOD -> 4;
            case VERY_GOOD -> 5;
            case EXCELLENT -> 6;
        };
    }

    private String formatAverage(double average) {
        return String.format(Locale.ENGLISH, "%.2f", average);
    }

    private String buildInitials(String firstName, String lastName) {
        String first = firstName != null && !firstName.isBlank() ? firstName.substring(0, 1).toUpperCase(Locale.ENGLISH) : "";
        String last = lastName != null && !lastName.isBlank() ? lastName.substring(0, 1).toUpperCase(Locale.ENGLISH) : "";
        String initials = first + last;
        return initials.isBlank() ? "--" : initials;
    }

    private String buildTeacherName(User teacher) {
        if (teacher == null) {
            return "--";
        }
        String first = teacher.getFirstName() != null ? teacher.getFirstName() : "";
        String last = teacher.getLastName() != null ? teacher.getLastName() : "";
        String fullName = (first + " " + last).trim();
        return fullName.isEmpty() ? teacher.getEmail() : fullName;
    }
}

