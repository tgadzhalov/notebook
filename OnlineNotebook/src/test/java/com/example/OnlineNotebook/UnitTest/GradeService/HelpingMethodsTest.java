package com.example.OnlineNotebook.UnitTest.GradeService;

import com.example.OnlineNotebook.models.entities.User;
import com.example.OnlineNotebook.models.enums.GradeLetter;
import com.example.OnlineNotebook.services.GradeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class HelpingMethodsTest {

    @InjectMocks
    private GradeService gradeService;


    @Test
    void resolveGradeValue_whenGradeLetterIsNull_thenReturnNull() throws Exception {
        Method method = GradeService.class.getDeclaredMethod("resolveGradeValue", GradeLetter.class);
        method.setAccessible(true);
        
        Integer result = (Integer) method.invoke(gradeService, (GradeLetter) null);
        
        assertNull(result);
    }

    @Test
    void resolveGradeValue_whenGradeLetterIsProvided_thenReturnCorrectValue() throws Exception {
        Method method = GradeService.class.getDeclaredMethod("resolveGradeValue", GradeLetter.class);
        method.setAccessible(true);
        
        assertEquals(2, method.invoke(gradeService, GradeLetter.BAD));
        assertEquals(3, method.invoke(gradeService, GradeLetter.AVERAGE));
        assertEquals(4, method.invoke(gradeService, GradeLetter.GOOD));
        assertEquals(5, method.invoke(gradeService, GradeLetter.VERY_GOOD));
        assertEquals(6, method.invoke(gradeService, GradeLetter.EXCELLENT));
    }

    @Test
    void formatTeacherName_whenUserIsNull_thenReturnNull() throws Exception {
        Method method = GradeService.class.getDeclaredMethod("formatTeacherName", User.class);
        method.setAccessible(true);
        
        String result = (String) method.invoke(gradeService, (User) null);
        
        assertNull(result);
    }

    @Test
    void formatTeacherName_whenUserIsProvided_thenReturnFormattedName() throws Exception {
        Method method = GradeService.class.getDeclaredMethod("formatTeacherName", User.class);
        method.setAccessible(true);
        
        assertEquals("John Doe", method.invoke(gradeService, User.builder().firstName("John").lastName("Doe").build()));
        assertEquals("Doe", method.invoke(gradeService, User.builder().firstName(null).lastName("Doe").build()));
        assertEquals("John", method.invoke(gradeService, User.builder().firstName("John").lastName(null).build()));
        assertEquals("", method.invoke(gradeService, User.builder().firstName(null).lastName(null).build()));
    }
}
