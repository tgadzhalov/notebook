package com.example.OnlineNotebook.UnitTest.GradeService;

import com.example.OnlineNotebook.exceptions.ResourceNotFoundException;
import com.example.OnlineNotebook.models.dtos.teacher.grade.SaveGradesDto;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Optional.empty;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaveGradesTest {
    @InjectMocks
    private GradeService gradeService;
    @Mock
    private CourseRepository courseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private GradeRepository gradeRepository;

    @Test
    void saveGrades_whenCourseNotFound_thenThrowResourceNotFoundException() {
        UUID courseId = UUID.randomUUID();
        SaveGradesDto saveGradesDto = SaveGradesDto.builder()
                .courseId(courseId)
                .assignmentId(UUID.randomUUID())
                .subjectType(SubjectType.BULGARIAN.name())
                .gradeDate(LocalDate.now())
                .studentGrades(null)
                .build();

        User teacher = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.TEACHER)
                .build();

        when(courseRepository.findById(courseId)).thenReturn(empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
                () -> gradeService.saveGrades(saveGradesDto, teacher));

        assertEquals("Course not found", exception.getMessage());
        verify(courseRepository, times(1)).findById(courseId);
        verify(userRepository, never()).findById(any());
        verify(gradeRepository, never()).findByStudent(any());
    }

    @Test
    void saveGrades_whenCourseHasNoSubjects_thenThrowIllegalArgumentException() {
        UUID courseId = UUID.randomUUID();
        SaveGradesDto saveGradesDto = SaveGradesDto.builder()
                .courseId(courseId)
                .assignmentId(UUID.randomUUID())
                .subjectType(SubjectType.BULGARIAN.name())
                .gradeDate(LocalDate.now())
                .studentGrades(null)
                .build();

        User teacher = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.TEACHER)
                .build();

        Course course = Course.builder()
                .id(courseId)
                .name("Maths")
                .teacher(teacher)
                .subjects(null)
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> gradeService.saveGrades(saveGradesDto, teacher));

        assertEquals("Course has no subjects", exception.getMessage());
        verify(courseRepository, times(1)).findById(courseId);
        verify(userRepository, never()).findById(any());
        verify(gradeRepository, never()).findByStudent(any());
    }

    @Test
    void saveGrades_whenCourseHasEmptySubjectsList_thenThrowIllegalArgumentException() {
        UUID courseId = UUID.randomUUID();
        SaveGradesDto saveGradesDto = SaveGradesDto.builder()
                .courseId(courseId)
                .assignmentId(UUID.randomUUID())
                .subjectType(SubjectType.BULGARIAN.name())
                .gradeDate(LocalDate.now())
                .studentGrades(null)
                .build();

        User teacher = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.TEACHER)
                .build();

        Course course = Course.builder()
                .id(courseId)
                .name("Maths")
                .teacher(teacher)
                .subjects(Collections.emptyList())
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> gradeService.saveGrades(saveGradesDto, teacher));

        assertEquals("Course has no subjects", exception.getMessage());
        verify(courseRepository, times(1)).findById(courseId);
        verify(userRepository, never()).findById(any());
        verify(gradeRepository, never()).findByStudent(any());
    }

    @Test
    void saveGrades_whenSubjectTypeIsNull_thenThrowIllegalArgumentException() {
        UUID courseId = UUID.randomUUID();
        UUID validAssignmentId = UUID.nameUUIDFromBytes("TEST".getBytes());
        
        SaveGradesDto saveGradesDto = SaveGradesDto.builder()
                .courseId(courseId)
                .assignmentId(validAssignmentId)
                .subjectType(null)
                .gradeDate(LocalDate.now())
                .studentGrades(null)
                .build();

        User teacher = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.TEACHER)
                .build();

        Course course = Course.builder()
                .id(courseId)
                .name("Maths")
                .teacher(teacher)
                .subjects(List.of(SubjectType.MATH))
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> gradeService.saveGrades(saveGradesDto, teacher));

        assertEquals("Subject type is required", exception.getMessage());
        verify(courseRepository, times(1)).findById(courseId);
        verify(userRepository, never()).findById(any());
        verify(gradeRepository, never()).findByStudent(any());
    }

    @Test
    void saveGrades_whenSubjectTypeIsEmpty_thenThrowIllegalArgumentException() {
        UUID courseId = UUID.randomUUID();
        UUID validAssignmentId = UUID.nameUUIDFromBytes("TEST".getBytes());
        
        SaveGradesDto saveGradesDto = SaveGradesDto.builder()
                .courseId(courseId)
                .assignmentId(validAssignmentId)
                .subjectType("")
                .gradeDate(LocalDate.now())
                .studentGrades(null)
                .build();

        User teacher = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.TEACHER)
                .build();

        Course course = Course.builder()
                .id(courseId)
                .name("Maths")
                .teacher(teacher)
                .subjects(List.of(SubjectType.MATH))
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> gradeService.saveGrades(saveGradesDto, teacher));

        assertEquals("Subject type is required", exception.getMessage());
        verify(courseRepository, times(1)).findById(courseId);
        verify(userRepository, never()).findById(any());
        verify(gradeRepository, never()).findByStudent(any());
    }

    @Test
    void saveGrades_whenNoGradeTypeMatches_thenThrowResourceNotFoundException() {
        UUID courseId = UUID.randomUUID();
        UUID invalidAssignmentId = UUID.randomUUID();
        
        SaveGradesDto saveGradesDto = SaveGradesDto.builder()
                .courseId(courseId)
                .assignmentId(invalidAssignmentId)
                .subjectType(SubjectType.SCIENCE.name())
                .gradeDate(LocalDate.now())
                .studentGrades(null)
                .build();

        User teacher = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.TEACHER)
                .build();

        Course course = Course.builder()
                .id(courseId)
                .name("Science")
                .teacher(teacher)
                .subjects(List.of(SubjectType.SCIENCE))
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> gradeService.saveGrades(saveGradesDto, teacher));

        assertEquals("Invalid grade type", exception.getMessage());
        verify(courseRepository, times(1)).findById(courseId);
        verify(userRepository, never()).findById(any());
        verify(gradeRepository, never()).findByStudent(any());
    }

    @Test
    void saveGrades_whenSubjectTypeIsInvalid_thenIllegalArgumentException() {
        UUID courseId = UUID.randomUUID();
        String invalidSubjectType = "INVALID_SUBJECT_TYPE";
        UUID validAssignmentId = UUID.nameUUIDFromBytes("TEST".getBytes());
        
        SaveGradesDto saveGradesDto = SaveGradesDto.builder()
                .courseId(courseId)
                .assignmentId(validAssignmentId)
                .subjectType(invalidSubjectType)
                .gradeDate(LocalDate.now())
                .studentGrades(null)
                .build();

        User teacher = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.TEACHER)
                .build();

        Course course = Course.builder()
                .id(courseId)
                .name("Maths")
                .teacher(teacher)
                .subjects(List.of(SubjectType.MATH))
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> gradeService.saveGrades(saveGradesDto, teacher));

        assertEquals("Invalid subject type: " + invalidSubjectType, exception.getMessage());
        verify(courseRepository, times(1)).findById(courseId);
        verify(userRepository, never()).findById(any());
        verify(gradeRepository, never()).findByStudent(any());
    }

    @Test
    void saveGrades_whenSubjectNotPartOfCourse_thenThrowIllegalArgumentException() {
        UUID courseId = UUID.randomUUID();
        UUID validAssignmentId = UUID.nameUUIDFromBytes("TEST".getBytes());
        
        SaveGradesDto saveGradesDto = SaveGradesDto.builder()
                .courseId(courseId)
                .assignmentId(validAssignmentId)
                .subjectType(SubjectType.ENGLISH.name())
                .gradeDate(LocalDate.now())
                .studentGrades(null)
                .build();

        User teacher = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.TEACHER)
                .build();

        Course course = Course.builder()
                .id(courseId)
                .name("Maths")
                .teacher(teacher)
                .subjects(List.of(SubjectType.MATH))
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> gradeService.saveGrades(saveGradesDto, teacher));

        assertEquals("Subject " + SubjectType.ENGLISH + " is not part of this course", exception.getMessage());
        verify(courseRepository, times(1)).findById(courseId);
        verify(userRepository, never()).findById(any());
        verify(gradeRepository, never()).findByStudent(any());
    }

    @Test
    void saveGrades_whenStudentNotFound_thenThrowResourceNotFoundException() {
        UUID courseId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID validAssignmentId = UUID.nameUUIDFromBytes("TEST".getBytes());
        
        Map<String, String> studentGrades = Map.of(studentId.toString(), "5");
        
        SaveGradesDto saveGradesDto = SaveGradesDto.builder()
                .courseId(courseId)
                .assignmentId(validAssignmentId)
                .subjectType(SubjectType.MATH.name())
                .gradeDate(LocalDate.now())
                .studentGrades(studentGrades)
                .build();

        User teacher = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.TEACHER)
                .build();

        Course course = Course.builder()
                .id(courseId)
                .name("Maths")
                .teacher(teacher)
                .subjects(List.of(SubjectType.MATH))
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(studentId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> gradeService.saveGrades(saveGradesDto, teacher));

        assertEquals("Student not found: " + studentId, exception.getMessage());
        verify(courseRepository, times(1)).findById(courseId);
        verify(userRepository, times(1)).findById(studentId);
        verify(gradeRepository, never()).findByStudent(any());
        verify(gradeRepository, never()).save(any());
    }

    @Test
    void saveGrades_whenCreatingNewGrade_thenShouldSaveGrade() {
        UUID courseId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID validAssignmentId = UUID.nameUUIDFromBytes("TEST".getBytes());
        
        Map<String, String> studentGrades = Map.of(studentId.toString(), "5");
        
        SaveGradesDto saveGradesDto = SaveGradesDto.builder()
                .courseId(courseId)
                .assignmentId(validAssignmentId)
                .subjectType(SubjectType.MATH.name())
                .gradeDate(LocalDate.now())
                .studentGrades(studentGrades)
                .build();

        User teacher = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.TEACHER)
                .build();

        User student = User.builder()
                .id(studentId)
                .userType(UserType.STUDENT)
                .build();

        Course course = Course.builder()
                .id(courseId)
                .name("Maths")
                .teacher(teacher)
                .subjects(List.of(SubjectType.MATH))
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(gradeRepository.findByStudent(student)).thenReturn(Collections.emptyList());
        when(gradeRepository.save(any(Grade.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> gradeService.saveGrades(saveGradesDto, teacher));

        verify(courseRepository, times(1)).findById(courseId);
        verify(userRepository, times(1)).findById(studentId);
        verify(gradeRepository, times(1)).findByStudent(student);
        verify(gradeRepository, times(1)).save(any(Grade.class));
        verify(gradeRepository, never()).delete(any());
    }

    @Test
    void saveGrades_whenUpdatingExistingGrade_thenShouldUpdateGrade() {
        UUID courseId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID gradeId = UUID.randomUUID();
        UUID validAssignmentId = UUID.nameUUIDFromBytes("TEST".getBytes());
        
        Map<String, String> studentGrades = Map.of(studentId.toString(), "6");
        
        SaveGradesDto saveGradesDto = SaveGradesDto.builder()
                .courseId(courseId)
                .assignmentId(validAssignmentId)
                .subjectType(SubjectType.MATH.name())
                .gradeDate(LocalDate.now())
                .studentGrades(studentGrades)
                .build();

        User teacher = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.TEACHER)
                .build();

        User student = User.builder()
                .id(studentId)
                .userType(UserType.STUDENT)
                .build();

        Course course = Course.builder()
                .id(courseId)
                .name("Maths")
                .teacher(teacher)
                .subjects(List.of(SubjectType.MATH))
                .build();

        Grade existingGrade = Grade.builder()
                .id(gradeId)
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now())
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(gradeRepository.findByStudent(student)).thenReturn(List.of(existingGrade));
        when(gradeRepository.save(any(Grade.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> gradeService.saveGrades(saveGradesDto, teacher));

        verify(courseRepository, times(1)).findById(courseId);
        verify(userRepository, times(1)).findById(studentId);
        verify(gradeRepository, times(1)).findByStudent(student);
        verify(gradeRepository, times(1)).save(existingGrade);
        verify(gradeRepository, never()).delete(any());
        assertEquals(GradeLetter.EXCELLENT, existingGrade.getGradeLetter());
    }

    @Test
    void saveGrades_whenGradeValueIsEmpty_thenShouldDeleteExistingGrade() {
        UUID courseId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID gradeId = UUID.randomUUID();
        UUID validAssignmentId = UUID.nameUUIDFromBytes("TEST".getBytes());
        
        Map<String, String> studentGrades = Map.of(studentId.toString(), "");
        
        SaveGradesDto saveGradesDto = SaveGradesDto.builder()
                .courseId(courseId)
                .assignmentId(validAssignmentId)
                .subjectType(SubjectType.MATH.name())
                .gradeDate(LocalDate.now())
                .studentGrades(studentGrades)
                .build();

        User teacher = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.TEACHER)
                .build();

        User student = User.builder()
                .id(studentId)
                .userType(UserType.STUDENT)
                .build();

        Course course = Course.builder()
                .id(courseId)
                .name("Maths")
                .teacher(teacher)
                .subjects(List.of(SubjectType.MATH))
                .build();

        Grade existingGrade = Grade.builder()
                .id(gradeId)
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now())
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(gradeRepository.findByStudent(student)).thenReturn(List.of(existingGrade));

        assertDoesNotThrow(() -> gradeService.saveGrades(saveGradesDto, teacher));

        verify(courseRepository, times(1)).findById(courseId);
        verify(userRepository, times(1)).findById(studentId);
        verify(gradeRepository, times(1)).findByStudent(student);
        verify(gradeRepository, times(1)).delete(existingGrade);
        verify(gradeRepository, never()).save(any());
    }

    @Test
    void saveGrades_whenGradeValueIsNull_thenShouldDeleteExistingGrade() {
        UUID courseId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID gradeId = UUID.randomUUID();
        UUID validAssignmentId = UUID.nameUUIDFromBytes("TEST".getBytes());
        
        Map<String, String> studentGrades = Map.of(studentId.toString(), "");
        
        SaveGradesDto saveGradesDto = SaveGradesDto.builder()
                .courseId(courseId)
                .assignmentId(validAssignmentId)
                .subjectType(SubjectType.MATH.name())
                .gradeDate(LocalDate.now())
                .studentGrades(studentGrades)
                .build();

        User teacher = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.TEACHER)
                .build();

        User student = User.builder()
                .id(studentId)
                .userType(UserType.STUDENT)
                .build();

        Course course = Course.builder()
                .id(courseId)
                .name("Maths")
                .teacher(teacher)
                .subjects(List.of(SubjectType.MATH))
                .build();

        Grade existingGrade = Grade.builder()
                .id(gradeId)
                .student(student)
                .subjectType(SubjectType.MATH)
                .gradeType(GradeType.TEST)
                .gradeLetter(GradeLetter.GOOD)
                .gradedBy(teacher)
                .dateGraded(LocalDateTime.now())
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(gradeRepository.findByStudent(student)).thenReturn(List.of(existingGrade));

        assertDoesNotThrow(() -> gradeService.saveGrades(saveGradesDto, teacher));

        verify(courseRepository, times(1)).findById(courseId);
        verify(userRepository, times(1)).findById(studentId);
        verify(gradeRepository, times(1)).findByStudent(student);
        verify(gradeRepository, times(1)).delete(existingGrade);
        verify(gradeRepository, never()).save(any());
    }

    @Test
    void saveGrades_whenGradeValueIsInvalid_thenShouldSkipGrade() {
        UUID courseId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID validAssignmentId = UUID.nameUUIDFromBytes("TEST".getBytes());
        
        Map<String, String> studentGrades = Map.of(studentId.toString(), "7");
        
        SaveGradesDto saveGradesDto = SaveGradesDto.builder()
                .courseId(courseId)
                .assignmentId(validAssignmentId)
                .subjectType(SubjectType.MATH.name())
                .gradeDate(LocalDate.now())
                .studentGrades(studentGrades)
                .build();

        User teacher = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.TEACHER)
                .build();

        User student = User.builder()
                .id(studentId)
                .userType(UserType.STUDENT)
                .build();

        Course course = Course.builder()
                .id(courseId)
                .name("Maths")
                .teacher(teacher)
                .subjects(List.of(SubjectType.MATH))
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(gradeRepository.findByStudent(student)).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> gradeService.saveGrades(saveGradesDto, teacher));

        verify(courseRepository, times(1)).findById(courseId);
        verify(userRepository, times(1)).findById(studentId);
        verify(gradeRepository, times(1)).findByStudent(student);
        verify(gradeRepository, never()).save(any());
        verify(gradeRepository, never()).delete(any());
    }

    @Test
    void saveGrades_whenStudentGradesMapIsEmpty_thenShouldNotProcessAnyGrades() {
        UUID courseId = UUID.randomUUID();
        UUID validAssignmentId = UUID.nameUUIDFromBytes("TEST".getBytes());
        
        SaveGradesDto saveGradesDto = SaveGradesDto.builder()
                .courseId(courseId)
                .assignmentId(validAssignmentId)
                .subjectType(SubjectType.MATH.name())
                .gradeDate(LocalDate.now())
                .studentGrades(Collections.emptyMap())
                .build();

        User teacher = User.builder()
                .id(UUID.randomUUID())
                .userType(UserType.TEACHER)
                .build();

        Course course = Course.builder()
                .id(courseId)
                .name("Maths")
                .teacher(teacher)
                .subjects(List.of(SubjectType.MATH))
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        assertDoesNotThrow(() -> gradeService.saveGrades(saveGradesDto, teacher));

        verify(courseRepository, times(1)).findById(courseId);
        verify(userRepository, never()).findById(any());
        verify(gradeRepository, never()).findByStudent(any());
        verify(gradeRepository, never()).save(any());
        verify(gradeRepository, never()).delete(any());
    }
}
