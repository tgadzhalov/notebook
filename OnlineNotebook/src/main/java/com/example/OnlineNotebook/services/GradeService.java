package com.example.OnlineNotebook.services;

import com.example.OnlineNotebook.exceptions.ResourceNotFoundException;
import com.example.OnlineNotebook.models.dtos.teacher.grade.GradeTypeOptionDto;
import com.example.OnlineNotebook.models.dtos.teacher.grade.GradingPageDto;
import com.example.OnlineNotebook.models.dtos.teacher.grade.SaveGradesDto;
import com.example.OnlineNotebook.models.dtos.teacher.grade.UpdateGradeFeedbackDto;
import com.example.OnlineNotebook.models.dtos.teacher.student.TeacherStudentGradeDto;
import com.example.OnlineNotebook.models.entities.Course;
import com.example.OnlineNotebook.models.entities.Grade;
import com.example.OnlineNotebook.models.entities.User;
import com.example.OnlineNotebook.models.enums.GradeLetter;
import com.example.OnlineNotebook.models.enums.GradeType;
import com.example.OnlineNotebook.models.enums.SubjectType;
import com.example.OnlineNotebook.models.enums.UserType;
import com.example.OnlineNotebook.repositories.CourseRepository;
import com.example.OnlineNotebook.repositories.GradeRepository;
import com.example.OnlineNotebook.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GradeService {
    
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final GradeRepository gradeRepository;
    
    public GradeService(CourseRepository courseRepository,
                       UserRepository userRepository,
                       GradeRepository gradeRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.gradeRepository = gradeRepository;
    }
    
    public GradingPageDto getGradingPageData(User teacher, UUID courseId, UUID assignmentId, String subjectType) {
        List<Course> courses = courseRepository.findByTeacher(teacher);
        
        List<GradeTypeOptionDto> assignments = Arrays.stream(GradeType.values())
                .map(gt -> new GradeTypeOptionDto(gt.name(), gt.getDisplayType()))
                .collect(Collectors.toList());
        
        List<User> students = List.of();
        Map<UUID, Grade> studentGrades = new HashMap<>();
        Course selectedCourse = null;
        List<SubjectType> subjects = List.of();
        GradeType selectedGradeType;
        SubjectType selectedSubjectEnum = null;
        
        if (courseId != null) {
            selectedCourse = courses.stream()
                    .filter(c -> c.getId().equals(courseId))
                    .findFirst()
                    .orElse(null);
            
            if (selectedCourse != null) {
                subjects = selectedCourse.getSubjects() != null ? selectedCourse.getSubjects() : List.of();
                
                students = userRepository.findByCourse(selectedCourse).stream()
                        .filter(u -> u.getUserType() == UserType.STUDENT)
                        .collect(Collectors.toList());
                
                if (subjectType != null && !subjectType.isEmpty()) {
                    try {
                        selectedSubjectEnum = SubjectType.valueOf(subjectType);
                    } catch (IllegalArgumentException e) {
                        selectedSubjectEnum = null;
                    }
                }
                
                if (assignmentId != null && selectedSubjectEnum != null) {
                    selectedGradeType = assignments.stream()
                            .filter(a -> a.getId().equals(assignmentId))
                            .findFirst()
                            .map(a -> GradeType.valueOf(a.getName()))
                            .orElse(null);
                    
                    if (selectedGradeType != null) {
                        for (User student : students) {
                            List<Grade> studentGradeList = gradeRepository.findByStudent(student);
                            SubjectType finalSelectedSubjectEnum = selectedSubjectEnum;
                            studentGradeList.stream()
                                    .filter(g -> g.getGradeType() == selectedGradeType && g.getSubjectType() == finalSelectedSubjectEnum)
                                    .findFirst()
                                    .ifPresent(grade -> studentGrades.put(student.getId(), grade));
                        }
                    }
                } else {
                    selectedGradeType = null;
                }
            }
        }

        return GradingPageDto.builder()
                .user(teacher)
                .courses(courses)
                .selectedCourse(selectedCourse)
                .selectedCourseId(courseId)
                .subjects(subjects)
                .selectedSubject(subjectType)
                .assignments(assignments)
                .selectedAssignmentId(assignmentId)
                .students(students)
                .studentGrades(studentGrades)
                .build();
    }
    
    @Transactional
    public void saveGrades(SaveGradesDto saveGradesDto, User teacher) {
        Course course = courseRepository.findById(saveGradesDto.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        
        if (course.getSubjects() == null || course.getSubjects().isEmpty()) {
            throw new IllegalArgumentException("Course has no subjects");
        }
        
        List<GradeTypeOptionDto> assignments = Arrays.stream(GradeType.values())
                .map(gt -> new GradeTypeOptionDto(gt.name(), gt.getDisplayType()))
                .collect(Collectors.toList());
        
        GradeType gradeType = assignments.stream()
                .filter(a -> a.getId().equals(saveGradesDto.getAssignmentId()))
                .findFirst()
                .map(a -> GradeType.valueOf(a.getName()))
                .orElseThrow(() -> new ResourceNotFoundException("Invalid grade type"));
        
        if (saveGradesDto.getSubjectType() == null || saveGradesDto.getSubjectType().isEmpty()) {
            throw new IllegalArgumentException("Subject type is required");
        }
        
        SubjectType subjectType;
        try {
            subjectType = SubjectType.valueOf(saveGradesDto.getSubjectType());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid subject type: " + saveGradesDto.getSubjectType());
        }
        
        if (!course.getSubjects().contains(subjectType)) {
            throw new IllegalArgumentException("Subject " + subjectType + " is not part of this course");
        }
        
        Map<String, GradeLetter> gradeValueToLetter = Map.of(
            "2", GradeLetter.BAD,
            "3", GradeLetter.AVERAGE,
            "4", GradeLetter.GOOD,
            "5", GradeLetter.VERY_GOOD,
            "6", GradeLetter.EXCELLENT
        );
        
        LocalDateTime dateGraded = saveGradesDto.getGradeDate().atStartOfDay();
        
        Map<UUID, String> studentGradesMap = saveGradesDto.getStudentGradesAsUuidMap();
        
        for (Map.Entry<UUID, String> entry : studentGradesMap.entrySet()) {
            UUID studentId = entry.getKey();
            String gradeValue = entry.getValue();
            
            User student = userRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
            
            List<Grade> existingGrades = gradeRepository.findByStudent(student);
            Grade existingGrade = existingGrades.stream()
                    .filter(g -> g.getSubjectType() == subjectType && g.getGradeType() == gradeType)
                    .findFirst()
                    .orElse(null);

            if (gradeValue == null || gradeValue.isEmpty()) {
                if (existingGrade != null) {
                    gradeRepository.delete(existingGrade);
                }
                continue;
            }

            GradeLetter gradeLetter = gradeValueToLetter.get(gradeValue);
            if (gradeLetter == null) {
                continue;
            }

            if (existingGrade != null) {
                existingGrade.setGradeLetter(gradeLetter);
                existingGrade.setDateGraded(dateGraded);
                existingGrade.setGradedBy(teacher);
                gradeRepository.save(existingGrade);
            } else {
                Grade grade = Grade.builder()
                        .student(student)
                        .subjectType(subjectType)
                        .gradeLetter(gradeLetter)
                        .gradeType(gradeType)
                        .gradedBy(teacher)
                        .dateGraded(dateGraded)
                        .build();
                gradeRepository.save(grade);
            }
        }
    }

    public List<TeacherStudentGradeDto> getStudentGradesForTeacher(User teacher, UUID studentId, UUID courseId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Course studentCourse = student.getCourse();
        if (studentCourse == null) {
            throw new IllegalArgumentException("Student is not assigned to a course");
        }

        Course course = studentCourse;

        if (courseId != null) {
            course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
            if (!course.getTeacher().getId().equals(teacher.getId())) {
                throw new IllegalArgumentException("You are not allowed to view grades for this course");
            }
            if (studentCourse.getId() == null || !Objects.equals(studentCourse.getId(), course.getId())) {
                throw new IllegalArgumentException("Student is not part of the selected course");
            }
        } else if (studentCourse.getTeacher() == null || !studentCourse.getTeacher().getId().equals(teacher.getId())) {
            throw new IllegalArgumentException("You are not allowed to view grades for this student");
        }

        List<SubjectType> courseSubjects = course.getSubjects() != null ? course.getSubjects() : List.of();

        return gradeRepository.findByStudent(student).stream()
                .filter(grade -> courseSubjects.isEmpty() || courseSubjects.contains(grade.getSubjectType()))
                .sorted(Comparator.comparing(Grade::getDateGraded).reversed())
                .map(grade -> TeacherStudentGradeDto.builder()
                        .gradeId(grade.getId())
                        .subjectCode(grade.getSubjectType().name())
                        .subject(grade.getSubjectType().getDisplayType())
                        .gradeTypeCode(grade.getGradeType().name())
                        .gradeType(grade.getGradeType().getDisplayType())
                        .gradeLetter(grade.getGradeLetter() != null ? grade.getGradeLetter().name() : null)
                        .gradeValue(resolveGradeValue(grade.getGradeLetter()))
                        .gradeDisplay(grade.getGradeLetter() != null ? grade.getGradeLetter().getDisplayType() : null)
                        .gradedOn(grade.getDateGraded().toLocalDate())
                        .gradedBy(formatTeacherName(grade.getGradedBy()))
                        .feedback(grade.getFeedback())
                        .build())
                .collect(Collectors.toList());
    }

    private Integer resolveGradeValue(GradeLetter gradeLetter) {
        if (gradeLetter == null) {
            return null;
        }
        return switch (gradeLetter) {
            case BAD -> 2;
            case AVERAGE -> 3;
            case GOOD -> 4;
            case VERY_GOOD -> 5;
            case EXCELLENT -> 6;
        };
    }

    private String formatTeacherName(User gradedBy) {
        if (gradedBy == null) {
            return null;
        }
        String firstName = gradedBy.getFirstName() != null ? gradedBy.getFirstName() : "";
        String lastName = gradedBy.getLastName() != null ? gradedBy.getLastName() : "";
        return (firstName + " " + lastName).trim();
    }

    @Transactional
    public void deleteGradeForTeacher(User teacher, UUID gradeId) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found"));

        User student = grade.getStudent();
        Course course = student != null ? student.getCourse() : null;

        boolean teacherOwnsCourse = course != null
                && course.getTeacher() != null
                && Objects.equals(course.getTeacher().getId(), teacher.getId());

        boolean teacherWasGrader = grade.getGradedBy() != null
                && Objects.equals(grade.getGradedBy().getId(), teacher.getId());

        if (!teacherOwnsCourse && !teacherWasGrader) {
            throw new IllegalArgumentException("You are not allowed to delete this grade");
        }

        gradeRepository.delete(grade);
    }

    @Transactional
    public void updateGradeFeedback(User teacher, UUID gradeId, UpdateGradeFeedbackDto updateDto) {
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new ResourceNotFoundException("Grade not found"));

        User student = grade.getStudent();
        Course course = student != null ? student.getCourse() : null;

        boolean teacherOwnsCourse = course != null
                && course.getTeacher() != null
                && Objects.equals(course.getTeacher().getId(), teacher.getId());

        boolean teacherWasGrader = grade.getGradedBy() != null
                && Objects.equals(grade.getGradedBy().getId(), teacher.getId());

        if (!teacherOwnsCourse && !teacherWasGrader) {
            throw new IllegalArgumentException("You are not allowed to update this grade");
        }

        String feedback = updateDto != null ? updateDto.getFeedback() : null;
        grade.setFeedback(feedback != null && !feedback.isBlank() ? feedback.trim() : null);
        gradeRepository.save(grade);
    }
}
