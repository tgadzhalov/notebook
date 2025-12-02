package com.example.OnlineNotebook.services;

import com.example.OnlineNotebook.exceptions.ResourceNotFoundException;
import com.example.OnlineNotebook.models.dtos.teacher.assignment.AssignmentFormDto;
import com.example.OnlineNotebook.models.dtos.teacher.assignment.AssignmentSummaryDto;
import com.example.OnlineNotebook.models.dtos.teacher.assignment.TeacherAssignmentsViewDto;
import com.example.OnlineNotebook.models.dtos.teacher.home.TeacherHomeViewDto;
import com.example.OnlineNotebook.models.dtos.teacher.student.TeacherStudentGradeDto;
import com.example.OnlineNotebook.models.dtos.teacher.student.TeacherStudentsViewDto;
import com.example.OnlineNotebook.models.entities.Assignment;
import com.example.OnlineNotebook.models.entities.Course;
import com.example.OnlineNotebook.models.entities.User;
import com.example.OnlineNotebook.models.enums.AssignmentType;
import com.example.OnlineNotebook.repositories.AssignmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TeacherService {
    private final CourseService courseService;
    private final UserService userService;
    private final AssignmentRepository assignmentRepository;
    private final GradeService gradeService;

    public TeacherService(CourseService courseService,
                          UserService userService,
                          AssignmentRepository assignmentRepository,
                          GradeService gradeService) {
        this.courseService = courseService;
        this.userService = userService;
        this.assignmentRepository = assignmentRepository;
        this.gradeService = gradeService;
    }

    public TeacherHomeViewDto buildHomeView(User teacher) {
        List<AssignmentSummaryDto> assignments = assignmentRepository
                .findByCreatedByOrderByAssignedDateDesc(teacher)
                .stream()
                .map(assignment -> AssignmentSummaryDto.builder()
                        .id(assignment.getId())
                        .title(assignment.getTitle())
                        .courseName(assignment.getCourse().getName())
                        .assignmentType(assignment.getType().getDisplayType())
                        .dueDate(assignment.getDueDate().toLocalDate())
                        .build())
                .collect(Collectors.toList());

        return TeacherHomeViewDto.builder()
                .assignments(assignments)
                .build();
    }

    public TeacherStudentsViewDto buildStudentsView(User teacher, UUID courseId, UUID studentId) {
        List<Course> courses = courseService.getCoursesByTeacher(teacher);

        UUID requestedCourseId = courseId;
        Course selectedCourse = null;

        if (requestedCourseId != null) {
            final UUID lookupCourseId = requestedCourseId;
            selectedCourse = courses.stream()
                    .filter(course -> course.getId().equals(lookupCourseId))
                    .findFirst()
                    .orElse(null);
        }

        UUID resolvedCourseId = requestedCourseId;

        if (selectedCourse == null && !courses.isEmpty()) {
            selectedCourse = courses.get(0);
            resolvedCourseId = selectedCourse.getId();
        }

        List<User> students = selectedCourse != null
                ? userService.getStudentsByCourse(selectedCourse)
                : List.of();

        List<Assignment> assignments = selectedCourse != null
                ? assignmentRepository.findByCourse(selectedCourse)
                : List.of();

        User selectedStudent = null;
        List<TeacherStudentGradeDto> studentGrades = Collections.emptyList();
        String gradeErrorMessage = null;
        boolean showModal = false;

        if (studentId != null) {
            selectedStudent = students.stream()
                    .filter(student -> student.getId().equals(studentId))
                    .findFirst()
                    .orElse(null);

            if (selectedStudent != null) {
                try {
                    studentGrades = gradeService.getStudentGradesForTeacher(
                            teacher,
                            selectedStudent.getId(),
                            resolvedCourseId);
                    showModal = true;
                } catch (ResourceNotFoundException | IllegalArgumentException ex) {
                    gradeErrorMessage = ex.getMessage();
                    studentGrades = Collections.emptyList();
                    showModal = true;
                }
            }
        }

        return TeacherStudentsViewDto.builder()
                .courses(courses)
                .selectedCourseId(resolvedCourseId)
                .students(students)
                .selectedStudentId(studentId)
                .selectedStudent(selectedStudent)
                .studentGrades(studentGrades)
                .showGradesModal(showModal)
                .gradeErrorMessage(gradeErrorMessage)
                .assignments(assignments)
                .build();
    }

    public TeacherAssignmentsViewDto buildAssignmentsView(User teacher) {
        List<Course> courses = courseService.getCoursesByTeacher(teacher);
        List<AssignmentType> assignmentTypes = Arrays.asList(AssignmentType.values());

        AssignmentFormDto form = AssignmentFormDto.builder()
                .courseId(!courses.isEmpty() ? courses.get(0).getId() : null)
                .title("")
                .description("")
                .assignmentType(!assignmentTypes.isEmpty() ? assignmentTypes.get(0) : null)
                .dueDate(LocalDate.now())
                .build();

        return TeacherAssignmentsViewDto.builder()
                .form(form)
                .courses(courses)
                .assignmentTypes(assignmentTypes)
                .build();
    }

    @CacheEvict(value = {"assignments", "studentHome"}, allEntries = true)
    public Assignment createAssignment(AssignmentFormDto assignmentFormDto, User teacher) {
        log.info("Creating assignment - teacherId: {}, title: {}, courseId: {}", 
            teacher.getId(), assignmentFormDto.getTitle(), assignmentFormDto.getCourseId());
        if (assignmentFormDto.getCourseId() == null) {
            throw new IllegalArgumentException("Course is required.");
        }

        Course course = courseService.getCourseById(assignmentFormDto.getCourseId());
        if (course == null || course.getTeacher() == null || !course.getTeacher().getId().equals(teacher.getId())) {
            throw new IllegalArgumentException("You are not allowed to create assignments for this course.");
        }

        if (assignmentFormDto.getAssignmentType() == null) {
            throw new IllegalArgumentException("Assignment type is required.");
        }

        if (assignmentFormDto.getDueDate() == null) {
            throw new IllegalArgumentException("Due date is required.");
        }

        Assignment assignment = Assignment.builder()
                .title(assignmentFormDto.getTitle())
                .description(assignmentFormDto.getDescription())
                .type(assignmentFormDto.getAssignmentType())
                .dueDate(assignmentFormDto.getDueDate().atStartOfDay())
                .assignedDate(LocalDateTime.now())
                .createdBy(teacher)
                .course(course)
                .build();

        Assignment savedAssignment = assignmentRepository.save(assignment);
        log.info("Assignment created successfully with id: {}", savedAssignment.getId());
        return savedAssignment;
    }

    @CacheEvict(value = {"assignments", "studentHome"}, allEntries = true)
    public void deleteAssignment(UUID assignmentId, User teacher) {
        log.info("Deleting assignment - teacherId: {}, assignmentId: {}", teacher.getId(), assignmentId);
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found."));

        if (!assignment.getCreatedBy().getId().equals(teacher.getId())) {
            throw new IllegalArgumentException("You are not allowed to delete this assignment.");
        }

        assignmentRepository.delete(assignment);
        log.info("Assignment deleted successfully - assignmentId: {}", assignmentId);
    }

}
