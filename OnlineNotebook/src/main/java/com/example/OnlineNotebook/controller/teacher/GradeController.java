package com.example.OnlineNotebook.controller.teacher;

import com.example.OnlineNotebook.models.dtos.teacher.grade.GradingPageDto;
import com.example.OnlineNotebook.models.dtos.teacher.grade.SaveGradesDto;
import com.example.OnlineNotebook.models.dtos.teacher.grade.UpdateGradeFeedbackDto;
import com.example.OnlineNotebook.models.dtos.teacher.student.TeacherStudentGradeDto;
import com.example.OnlineNotebook.models.entities.User;
import com.example.OnlineNotebook.security.UserData;
import com.example.OnlineNotebook.services.GradeService;
import com.example.OnlineNotebook.services.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@Controller
public class GradeController {
    private final UserService userService;
    private final GradeService gradeService;

    public GradeController(UserService userService, GradeService gradeService) {
        this.userService = userService;
        this.gradeService = gradeService;
    }

    @GetMapping("/teacher/grades")
    public ModelAndView grades(@AuthenticationPrincipal UserData userData,
                               @RequestParam(required = false) UUID courseId,
                               @RequestParam(required = false) UUID assignmentId,
                               @RequestParam(required = false) String subjectType) {
        User teacher = userService.getById(userData.getId());
        GradingPageDto gradingData = gradeService.getGradingPageData(teacher, courseId, assignmentId, subjectType);

        ModelAndView modelAndView = new ModelAndView("teacher/grades");
        modelAndView.addObject("user", gradingData.getUser());
        modelAndView.addObject("courses", gradingData.getCourses());
        modelAndView.addObject("selectedCourse", gradingData.getSelectedCourse());
        modelAndView.addObject("selectedCourseId", gradingData.getSelectedCourseId());
        modelAndView.addObject("subjects", gradingData.getSubjects());
        modelAndView.addObject("selectedSubject", gradingData.getSelectedSubject());
        modelAndView.addObject("assignments", gradingData.getAssignments());
        modelAndView.addObject("selectedAssignmentId", gradingData.getSelectedAssignmentId());
        modelAndView.addObject("students", gradingData.getStudents());
        modelAndView.addObject("studentGrades", gradingData.getStudentGrades());

        return modelAndView;
    }

    @PostMapping("/api/v1/teacher/grades")
    public String saveGrades(@AuthenticationPrincipal UserData userData, @Valid SaveGradesDto saveGradesDto, RedirectAttributes redirectAttributes) {

        User teacher = userService.getById(userData.getId());
        gradeService.saveGrades(saveGradesDto, teacher);
        redirectAttributes.addFlashAttribute("successMessage", "Grades saved successfully!");
        
        return "redirect:/teacher/grades?courseId=" + saveGradesDto.getCourseId() + 
               "&assignmentId=" + saveGradesDto.getAssignmentId() +
               "&subjectType=" + saveGradesDto.getSubjectType();
    }

    @GetMapping("/api/v1/teacher/students/{studentId}/grades")
    @ResponseBody
    public ResponseEntity<?> getStudentGrades(@AuthenticationPrincipal UserData userData,
                                              @PathVariable UUID studentId,
                                              @RequestParam(required = false) UUID courseId) {
        User teacher = userService.getById(userData.getId());
        List<TeacherStudentGradeDto> grades = gradeService.getStudentGradesForTeacher(teacher, studentId, courseId);
        return ResponseEntity.ok(grades);
    }

    @DeleteMapping("/api/v1/teacher/grades/{gradeId}")
    @ResponseBody
    public ResponseEntity<?> deleteGrade(@AuthenticationPrincipal UserData userData,
                                         @PathVariable UUID gradeId) {
        User teacher = userService.getById(userData.getId());
        gradeService.deleteGradeForTeacher(teacher, gradeId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/teacher/grades/{gradeId}/delete")
    public String deleteGradeViaForm(@AuthenticationPrincipal UserData userData,
                                     @PathVariable UUID gradeId,
                                     @RequestParam UUID studentId,
                                     @RequestParam(required = false) UUID courseId,
                                     RedirectAttributes redirectAttributes) {
        if (studentId != null) {
            redirectAttributes.addAttribute("studentId", studentId);
        }
        if (courseId != null) {
            redirectAttributes.addAttribute("courseId", courseId);
        }

        User teacher = userService.getById(userData.getId());
        gradeService.deleteGradeForTeacher(teacher, gradeId);
        redirectAttributes.addAttribute("successMessage", "Grade deleted successfully.");

        return "redirect:/teacher/students";
    }

    @PostMapping("/teacher/grades/{gradeId}/feedback")
    public String updateGradeFeedback(@AuthenticationPrincipal UserData userData,
                                      @PathVariable UUID gradeId,
                                      @Valid UpdateGradeFeedbackDto updateGradeFeedbackDto,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes) {
        UUID studentId = updateGradeFeedbackDto.getStudentId();
        UUID courseId = updateGradeFeedbackDto.getCourseId();

        if (studentId != null) {
            redirectAttributes.addAttribute("studentId", studentId);
        }
        if (courseId != null) {
            redirectAttributes.addAttribute("courseId", courseId);
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addAttribute("errorMessage", "Feedback must be 500 characters or fewer.");
            return "redirect:/teacher/students";
        }

        User teacher = userService.getById(userData.getId());
        gradeService.updateGradeFeedback(teacher, gradeId, updateGradeFeedbackDto);
        redirectAttributes.addAttribute("successMessage", "Feedback saved successfully.");

        return "redirect:/teacher/students";
    }
}
