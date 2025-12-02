package com.example.OnlineNotebook.services;

import com.example.OnlineNotebook.models.dtos.student.home.StudentAssignmentPreviewDto;
import com.example.OnlineNotebook.models.dtos.student.home.StudentGradePreviewDto;
import com.example.OnlineNotebook.models.dtos.student.home.StudentHomeViewDto;
import com.example.OnlineNotebook.models.dtos.student.home.StudentLeaderboardEntryDto;
import com.example.OnlineNotebook.models.dtos.student.home.StudentProfileDto;
import com.example.OnlineNotebook.models.dtos.student.home.StudentQuickStatsDto;
import com.example.OnlineNotebook.models.dtos.student.home.StudentSubjectGradeDto;
import com.example.OnlineNotebook.models.entities.Assignment;
import com.example.OnlineNotebook.models.entities.Course;
import com.example.OnlineNotebook.models.entities.Grade;
import com.example.OnlineNotebook.models.entities.User;
import com.example.OnlineNotebook.models.enums.AssignmentType;
import com.example.OnlineNotebook.models.enums.GradeLetter;
import com.example.OnlineNotebook.models.enums.SubjectType;
import com.example.OnlineNotebook.repositories.AssignmentRepository;
import com.example.OnlineNotebook.repositories.GradeRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class StudentHomeService {

    private static final DateTimeFormatter DUE_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);

    private final UserService userService;
    private final GradeRepository gradeRepository;
    private final AssignmentRepository assignmentRepository;

    public StudentHomeService(UserService userService,
                              GradeRepository gradeRepository,
                              AssignmentRepository assignmentRepository) {
        this.userService = userService;
        this.gradeRepository = gradeRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Cacheable(value = "studentHome", key = "#studentId")
    public StudentHomeViewDto buildHomeView(UUID studentId) {
        User student = userService.getById(studentId);
        List<Grade> grades = gradeRepository.findByStudent(student);
        Course course = student.getCourse();
        List<Assignment> assignments = course != null ? assignmentRepository.findByCourse(course) : List.of();

        return StudentHomeViewDto.builder()
                .profile(buildProfile(student))
                .quickStats(buildQuickStats(grades, assignments))
                .recentGrades(buildRecentGrades(grades))
                .upcomingAssignments(buildUpcomingAssignments(assignments))
                .subjectGrades(buildSubjectGrades(grades))
                .leaderboard(buildLeaderboard(course, assignments, student))
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

    private StudentQuickStatsDto buildQuickStats(List<Grade> grades, List<Assignment> assignments) {
        double averageGrade = grades.stream()
                .map(Grade::getGradeLetter)
                .map(this::resolveGradeValue)
                .filter(value -> value > 0)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(Double.NaN);

        int pendingAssignments = (int) assignments.stream()
                .filter(assignment -> assignment.getDueDate().isAfter(LocalDateTime.now()))
                .count();

        String attendanceDisplay = buildAttendanceDisplay(assignments.size(), grades.size());

        return StudentQuickStatsDto.builder()
                .averageGradeDisplay(Double.isNaN(averageGrade) ? "--" : formatAverage(averageGrade))
                .attendanceDisplay(attendanceDisplay)
                .pendingAssignments(pendingAssignments)
                .build();
    }

    private List<StudentGradePreviewDto> buildRecentGrades(List<Grade> grades) {
        return grades.stream()
                .sorted(Comparator.comparing(Grade::getDateGraded).reversed())
                .limit(3)
                .map(grade -> StudentGradePreviewDto.builder()
                        .subject(grade.getSubjectType() != null ? grade.getSubjectType().getDisplayType() : "General")
                        .assignment(grade.getGradeType() != null ? grade.getGradeType().getDisplayType() : "Assessment")
                        .gradeValue(resolveGradeValue(grade.getGradeLetter()))
                        .gradeLetter(grade.getGradeLetter() != null ? grade.getGradeLetter().name() : null)
                        .build())
                .collect(Collectors.toList());
    }

    private List<StudentAssignmentPreviewDto> buildUpcomingAssignments(List<Assignment> assignments) {
        LocalDateTime now = LocalDateTime.now();
        return assignments.stream()
                .filter(assignment -> !assignment.getDueDate().isBefore(now))
                .sorted(Comparator.comparing(Assignment::getDueDate))
                .limit(3)
                .map(assignment -> StudentAssignmentPreviewDto.builder()
                        .title(assignment.getTitle())
                        .subject(resolveAssignmentSubject(assignment.getType(), assignment.getCourse()))
                        .dueDateText(assignment.getDueDate().format(DUE_DATE_FORMATTER))
                        .priorityLabel(resolvePriorityLabel(assignment.getDueDate()))
                        .priorityLevel(resolvePriorityLevel(assignment.getDueDate()))
                        .build())
                .collect(Collectors.toList());
    }

    private List<StudentSubjectGradeDto> buildSubjectGrades(List<Grade> grades) {
        Map<SubjectType, List<Grade>> grouped = grades.stream()
                .filter(grade -> grade.getSubjectType() != null)
                .collect(Collectors.groupingBy(Grade::getSubjectType));

        return grouped.entrySet().stream()
                .map(entry -> {
                    double avg = entry.getValue().stream()
                            .map(Grade::getGradeLetter)
                            .map(this::resolveGradeValue)
                            .filter(value -> value > 0)
                            .mapToInt(Integer::intValue)
                            .average()
                            .orElse(Double.NaN);
                    int percentage = Double.isNaN(avg) ? 0 : (int) Math.round((avg / 6.0) * 100);
                    return StudentSubjectGradeDto.builder()
                            .subject(entry.getKey().getDisplayType())
                            .subjectCode(entry.getKey().name())
                            .gradeDisplay(Double.isNaN(avg) ? "--" : formatAverage(avg) + " (" + percentage + "%)")
                            .percentage(percentage)
                            .build();
                })
                .sorted(Comparator.comparing(StudentSubjectGradeDto::getPercentage).reversed())
                .collect(Collectors.toList());
    }

    private List<StudentLeaderboardEntryDto> buildLeaderboard(Course course,
                                                             List<Assignment> assignments,
                                                             User currentStudent) {
        if (course == null) {
            return List.of();
        }

        int totalAssignments = assignments.size();
        Comparator<LeaderboardProjection> comparator = Comparator
                .comparingDouble((LeaderboardProjection projection) -> Double.isNaN(projection.average()) ? 0 : projection.average())
                .reversed();

        AtomicInteger rankCounter = new AtomicInteger(1);

        return userService.getStudentsByCourse(course).stream()
                .map(student -> {
                    List<Grade> studentGrades = gradeRepository.findByStudent(student);
                    double average = studentGrades.stream()
                            .map(Grade::getGradeLetter)
                            .map(this::resolveGradeValue)
                            .filter(value -> value > 0)
                            .mapToInt(Integer::intValue)
                            .average()
                            .orElse(Double.NaN);
                    return new LeaderboardProjection(
                            student,
                            studentGrades.size(),
                            average,
                            buildAbsencesDisplay(totalAssignments, studentGrades.size())
                    );
                })
                .sorted(comparator)
                .limit(5)
                .map(projection -> StudentLeaderboardEntryDto.builder()
                        .rank(rankCounter.getAndIncrement())
                        .initials(buildInitials(projection.student().getFirstName(), projection.student().getLastName()))
                        .fullName(buildFullName(projection.student()))
                        .className(projection.student().getStudentClass())
                        .gradeDisplay(Double.isNaN(projection.average()) ? "--" : formatAverage(projection.average()))
                        .attendanceDisplay(projection.absencesDisplay())
                        .currentUser(projection.student().getId().equals(currentStudent.getId()))
                        .build())
                .collect(Collectors.toList());
    }

    private String resolveAssignmentSubject(AssignmentType type, Course course) {
        if (type != null) {
            return type.getDisplayType();
        }
        return course != null ? course.getName() : "Course";
    }

    private String resolvePriorityLabel(LocalDateTime dueDate) {
        long daysRemaining = Duration.between(LocalDateTime.now(), dueDate).toDays();
        if (daysRemaining <= 2) {
            return "High";
        } else if (daysRemaining <= 5) {
            return "Medium";
        }
        return "Low";
    }

    private String resolvePriorityLevel(LocalDateTime dueDate) {
        long daysRemaining = Duration.between(LocalDateTime.now(), dueDate).toDays();
        if (daysRemaining <= 2) {
            return "high";
        } else if (daysRemaining <= 5) {
            return "medium";
        }
        return "low";
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
        return (first + last).isBlank() ? "--" : first + last;
    }

    private String buildFullName(User user) {
        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";
        return (firstName + " " + lastName).trim();
    }

    private String buildAttendanceDisplay(int totalAssignments, int gradedEntries) {
        if (totalAssignments <= 0) {
            return "--";
        }
        double ratio = Math.min(1d, (double) gradedEntries / totalAssignments);
        return String.format(Locale.ENGLISH, "%.0f%%", ratio * 100);
    }

    private String buildAbsencesDisplay(int totalAssignments, int gradedEntries) {
        if (totalAssignments <= 0) {
            return "--";
        }
        int absences = Math.max(0, totalAssignments - gradedEntries);
        return String.valueOf(absences);
    }

    private record LeaderboardProjection(User student,
                                         int gradedEntries,
                                         double average,
                                         String absencesDisplay) {
    }
}

