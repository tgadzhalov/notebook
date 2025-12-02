package com.example.OnlineNotebook.UnitTest.GradeService;

import com.example.OnlineNotebook.exceptions.ResourceNotFoundException;
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
import com.example.OnlineNotebook.services.GradeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteGradeForTeacherTest {

    @InjectMocks
    private GradeService gradeService;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GradeRepository gradeRepository;

    @Test
    void deleteGradeForTeacher_whenGradeNotFound_thenThrowResourceNotFoundException() {
        UUID gradeId = UUID.randomUUID();
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.TEACHER)
                .build();

        when(gradeRepository.findById(gradeId)).thenReturn(empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> gradeService.deleteGradeForTeacher(teacher, gradeId));

        assertEquals("Grade not found", exception.getMessage());
        verify(gradeRepository, times(1)).findById(gradeId);
        verify(gradeRepository, never()).delete(any());
    }

    @Test
    void deleteGradeForTeacher_whenTeacherOwnsCourse_thenShouldDeleteGrade() {
        UUID teacherId = UUID.randomUUID();
        UUID gradeId = UUID.randomUUID();
        
        User teacher = User.builder()
                .id(teacherId)
                .userType(UserType.TEACHER)
                .build();

        User student = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.STUDENT)
                .build();

        Course course = Course.builder()
                .id(UUID.randomUUID())
                .teacher(teacher)
                .build();

        student.setCourse(course);

        Grade grade = Grade.builder()
                .id(gradeId)
                .student(student)
                .gradedBy(User.builder().id(UUID.randomUUID()).build())
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .dateGraded(LocalDateTime.now())
                .build();

        when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));

        assertDoesNotThrow(() -> gradeService.deleteGradeForTeacher(teacher, gradeId));

        verify(gradeRepository, times(1)).findById(gradeId);
        verify(gradeRepository, times(1)).delete(grade);
    }

    @Test
    void deleteGradeForTeacher_whenTeacherWasGrader_thenShouldDeleteGrade() {
        UUID teacherId = UUID.randomUUID();
        UUID gradeId = UUID.randomUUID();
        
        User teacher = User.builder()
                .id(teacherId)
                .userType(UserType.TEACHER)
                .build();

        User otherTeacher = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.TEACHER)
                .build();

        User student = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.STUDENT)
                .build();

        Course course = Course.builder()
                .id(UUID.randomUUID())
                .teacher(otherTeacher)
                .build();

        student.setCourse(course);

        Grade grade = Grade.builder()
                .id(gradeId)
                .student(student)
                .gradedBy(teacher)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .dateGraded(LocalDateTime.now())
                .build();

        when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));

        assertDoesNotThrow(() -> gradeService.deleteGradeForTeacher(teacher, gradeId));

        verify(gradeRepository, times(1)).findById(gradeId);
        verify(gradeRepository, times(1)).delete(grade);
    }

    @Test
    void deleteGradeForTeacher_whenTeacherNeitherOwnsCourseNorWasGrader_thenThrowIllegalArgumentException() {
        UUID teacherId = UUID.randomUUID();
        UUID otherTeacherId = UUID.randomUUID();
        UUID gradeId = UUID.randomUUID();
        
        User teacher = User.builder()
                .id(teacherId)
                .userType(UserType.TEACHER)
                .build();

        User otherTeacher = User.builder()
                .id(otherTeacherId)
                .userType(UserType.TEACHER)
                .build();

        User student = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.STUDENT)
                .build();

        Course course = Course.builder()
                .id(UUID.randomUUID())
                .teacher(otherTeacher)
                .build();

        student.setCourse(course);

        Grade grade = Grade.builder()
                .id(gradeId)
                .student(student)
                .gradedBy(otherTeacher)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .dateGraded(LocalDateTime.now())
                .build();

        when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> gradeService.deleteGradeForTeacher(teacher, gradeId));

        assertEquals("You are not allowed to delete this grade", exception.getMessage());
        verify(gradeRepository, times(1)).findById(gradeId);
        verify(gradeRepository, never()).delete(any());
    }

    @Test
    void deleteGradeForTeacher_whenStudentIsNull_thenThrowIllegalArgumentException() {
        UUID teacherId = UUID.randomUUID();
        UUID otherTeacherId = UUID.randomUUID();
        UUID gradeId = UUID.randomUUID();
        
        User teacher = User.builder()
                .id(teacherId)
                .userType(UserType.TEACHER)
                .build();

        User otherTeacher = User.builder()
                .id(otherTeacherId)
                .userType(UserType.TEACHER)
                .build();

        Grade grade = Grade.builder()
                .id(gradeId)
                .student(null)
                .gradedBy(otherTeacher)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .dateGraded(LocalDateTime.now())
                .build();

        when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> gradeService.deleteGradeForTeacher(teacher, gradeId));

        assertEquals("You are not allowed to delete this grade", exception.getMessage());
        verify(gradeRepository, times(1)).findById(gradeId);
        verify(gradeRepository, never()).delete(any());
    }

    @Test
    void deleteGradeForTeacher_whenCourseIsNull_thenThrowIllegalArgumentException() {
        UUID teacherId = UUID.randomUUID();
        UUID gradeId = UUID.randomUUID();
        
        User teacher = User.builder()
                .id(teacherId)
                .userType(UserType.TEACHER)
                .build();

        User student = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.STUDENT)
                .course(null)
                .build();

        Grade grade = Grade.builder()
                .id(gradeId)
                .student(student)
                .gradedBy(User.builder().id(UUID.randomUUID()).build())
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .dateGraded(LocalDateTime.now())
                .build();

        when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> gradeService.deleteGradeForTeacher(teacher, gradeId));

        assertEquals("You are not allowed to delete this grade", exception.getMessage());
        verify(gradeRepository, times(1)).findById(gradeId);
        verify(gradeRepository, never()).delete(any());
    }

    @Test
    void deleteGradeForTeacher_whenCourseTeacherIsNull_thenThrowIllegalArgumentException() {
        UUID teacherId = UUID.randomUUID();
        UUID gradeId = UUID.randomUUID();
        
        User teacher = User.builder()
                .id(teacherId)
                .userType(UserType.TEACHER)
                .build();

        User student = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.STUDENT)
                .build();

        Course course = Course.builder()
                .id(UUID.randomUUID())
                .teacher(null)
                .build();

        student.setCourse(course);

        Grade grade = Grade.builder()
                .id(gradeId)
                .student(student)
                .gradedBy(User.builder().id(UUID.randomUUID()).build())
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .dateGraded(LocalDateTime.now())
                .build();

        when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> gradeService.deleteGradeForTeacher(teacher, gradeId));

        assertEquals("You are not allowed to delete this grade", exception.getMessage());
        verify(gradeRepository, times(1)).findById(gradeId);
        verify(gradeRepository, never()).delete(any());
    }
}
