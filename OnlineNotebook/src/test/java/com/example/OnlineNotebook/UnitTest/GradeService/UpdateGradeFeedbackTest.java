package com.example.OnlineNotebook.UnitTest.GradeService;

import com.example.OnlineNotebook.exceptions.ResourceNotFoundException;
import com.example.OnlineNotebook.models.dtos.teacher.grade.UpdateGradeFeedbackDto;
import com.example.OnlineNotebook.models.entities.Course;
import com.example.OnlineNotebook.models.entities.Grade;
import com.example.OnlineNotebook.models.entities.User;
import com.example.OnlineNotebook.models.enums.GradeLetter;
import com.example.OnlineNotebook.models.enums.GradeType;
import com.example.OnlineNotebook.models.enums.SubjectType;
import com.example.OnlineNotebook.models.enums.UserType;
import com.example.OnlineNotebook.repositories.GradeRepository;
import com.example.OnlineNotebook.services.GradeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class UpdateGradeFeedbackTest {

    @InjectMocks
    private GradeService gradeService;
    @Mock
    private GradeRepository gradeRepository;

    @Test
    void updateGradeFeedback_whenGradeNotFound_thenThrowResourceNotFoundException() {
        UUID gradeId = UUID.randomUUID();
        User teacher = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.TEACHER)
                .build();

        UpdateGradeFeedbackDto updateDto = UpdateGradeFeedbackDto.builder()
                .feedback("Good work")
                .build();

        when(gradeRepository.findById(gradeId)).thenReturn(empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> gradeService.updateGradeFeedback(teacher, gradeId, updateDto));

        assertEquals("Grade not found", exception.getMessage());
        verify(gradeRepository, times(1)).findById(gradeId);
        verify(gradeRepository, never()).save(any());
    }

    @Test
    void updateGradeFeedback_whenTeacherOwnsCourse_thenShouldUpdateFeedback() {
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
                .feedback("Old feedback")
                .build();

        UpdateGradeFeedbackDto updateDto = UpdateGradeFeedbackDto.builder()
                .feedback("New feedback")
                .build();

        when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));
        when(gradeRepository.save(any(Grade.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> gradeService.updateGradeFeedback(teacher, gradeId, updateDto));

        ArgumentCaptor<Grade> gradeCaptor = ArgumentCaptor.forClass(Grade.class);
        verify(gradeRepository, times(1)).findById(gradeId);
        verify(gradeRepository, times(1)).save(gradeCaptor.capture());
        assertEquals("New feedback", gradeCaptor.getValue().getFeedback());
    }

    @Test
    void updateGradeFeedback_whenTeacherWasGrader_thenShouldUpdateFeedback() {
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
                .feedback("Old feedback")
                .build();

        UpdateGradeFeedbackDto updateDto = UpdateGradeFeedbackDto.builder()
                .feedback("New feedback")
                .build();

        when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));
        when(gradeRepository.save(any(Grade.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> gradeService.updateGradeFeedback(teacher, gradeId, updateDto));

        ArgumentCaptor<Grade> gradeCaptor = ArgumentCaptor.forClass(Grade.class);
        verify(gradeRepository, times(1)).findById(gradeId);
        verify(gradeRepository, times(1)).save(gradeCaptor.capture());
        assertEquals("New feedback", gradeCaptor.getValue().getFeedback());
    }

    @Test
    void updateGradeFeedback_whenTeacherNeitherOwnsCourseNorWasGrader_thenThrowIllegalArgumentException() {
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

        UpdateGradeFeedbackDto updateDto = UpdateGradeFeedbackDto.builder()
                .feedback("New feedback")
                .build();

        when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> gradeService.updateGradeFeedback(teacher, gradeId, updateDto));

        assertEquals("You are not allowed to update this grade", exception.getMessage());
        verify(gradeRepository, times(1)).findById(gradeId);
        verify(gradeRepository, never()).save(any());
    }

    @Test
    void updateGradeFeedback_whenStudentIsNull_thenThrowIllegalArgumentException() {
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

        UpdateGradeFeedbackDto updateDto = UpdateGradeFeedbackDto.builder()
                .feedback("New feedback")
                .build();

        when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> gradeService.updateGradeFeedback(teacher, gradeId, updateDto));

        assertEquals("You are not allowed to update this grade", exception.getMessage());
        verify(gradeRepository, times(1)).findById(gradeId);
        verify(gradeRepository, never()).save(any());
    }

    @Test
    void updateGradeFeedback_whenCourseIsNull_thenThrowIllegalArgumentException() {
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

        UpdateGradeFeedbackDto updateDto = UpdateGradeFeedbackDto.builder()
                .feedback("New feedback")
                .build();

        when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> gradeService.updateGradeFeedback(teacher, gradeId, updateDto));

        assertEquals("You are not allowed to update this grade", exception.getMessage());
        verify(gradeRepository, times(1)).findById(gradeId);
        verify(gradeRepository, never()).save(any());
    }

    @Test
    void updateGradeFeedback_whenCourseTeacherIsNull_thenThrowIllegalArgumentException() {
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

        UpdateGradeFeedbackDto updateDto = UpdateGradeFeedbackDto.builder()
                .feedback("New feedback")
                .build();

        when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> gradeService.updateGradeFeedback(teacher, gradeId, updateDto));

        assertEquals("You are not allowed to update this grade", exception.getMessage());
        verify(gradeRepository, times(1)).findById(gradeId);
        verify(gradeRepository, never()).save(any());
    }

    @Test
    void updateGradeFeedback_whenUpdateDtoIsNull_thenShouldSetFeedbackToNull() {
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
                .gradedBy(teacher)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .dateGraded(LocalDateTime.now())
                .feedback("Old feedback")
                .build();

        when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));
        when(gradeRepository.save(any(Grade.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> gradeService.updateGradeFeedback(teacher, gradeId, null));

        ArgumentCaptor<Grade> gradeCaptor = ArgumentCaptor.forClass(Grade.class);
        verify(gradeRepository, times(1)).findById(gradeId);
        verify(gradeRepository, times(1)).save(gradeCaptor.capture());
        assertNull(gradeCaptor.getValue().getFeedback());
    }

    @Test
    void updateGradeFeedback_whenFeedbackIsNull_thenShouldSetFeedbackToNull() {
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
                .gradedBy(teacher)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .dateGraded(LocalDateTime.now())
                .feedback("Old feedback")
                .build();

        UpdateGradeFeedbackDto updateDto = UpdateGradeFeedbackDto.builder()
                .feedback(null)
                .build();

        when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));
        when(gradeRepository.save(any(Grade.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> gradeService.updateGradeFeedback(teacher, gradeId, updateDto));

        ArgumentCaptor<Grade> gradeCaptor = ArgumentCaptor.forClass(Grade.class);
        verify(gradeRepository, times(1)).findById(gradeId);
        verify(gradeRepository, times(1)).save(gradeCaptor.capture());
        assertNull(gradeCaptor.getValue().getFeedback());
    }

    @Test
    void updateGradeFeedback_whenFeedbackIsBlank_thenShouldSetFeedbackToNull() {
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
                .gradedBy(teacher)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .dateGraded(LocalDateTime.now())
                .feedback("Old feedback")
                .build();

        UpdateGradeFeedbackDto updateDto = UpdateGradeFeedbackDto.builder()
                .feedback("   ")
                .build();

        when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));
        when(gradeRepository.save(any(Grade.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> gradeService.updateGradeFeedback(teacher, gradeId, updateDto));

        ArgumentCaptor<Grade> gradeCaptor = ArgumentCaptor.forClass(Grade.class);
        verify(gradeRepository, times(1)).findById(gradeId);
        verify(gradeRepository, times(1)).save(gradeCaptor.capture());
        assertNull(gradeCaptor.getValue().getFeedback());
    }

    @Test
    void updateGradeFeedback_whenFeedbackHasWhitespace_thenShouldTrimFeedback() {
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
                .gradedBy(teacher)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .dateGraded(LocalDateTime.now())
                .build();

        UpdateGradeFeedbackDto updateDto = UpdateGradeFeedbackDto.builder()
                .feedback("  Good work  ")
                .build();

        when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(grade));
        when(gradeRepository.save(any(Grade.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> gradeService.updateGradeFeedback(teacher, gradeId, updateDto));

        ArgumentCaptor<Grade> gradeCaptor = ArgumentCaptor.forClass(Grade.class);
        verify(gradeRepository, times(1)).findById(gradeId);
        verify(gradeRepository, times(1)).save(gradeCaptor.capture());
        assertEquals("Good work", gradeCaptor.getValue().getFeedback());
    }
}









