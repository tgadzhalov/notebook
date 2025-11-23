package com.example.OnlineNotebook.controller.teacher;

import com.example.OnlineNotebook.models.dtos.auth.EditProfileDto;
import com.example.OnlineNotebook.models.dtos.teacher.student.TeacherStudentsViewDto;
import com.example.OnlineNotebook.models.entities.User;
import com.example.OnlineNotebook.security.UserData;
import com.example.OnlineNotebook.services.AttendanceService;
import com.example.OnlineNotebook.services.TeacherService;
import com.example.OnlineNotebook.services.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    private final UserService userService;
    private final TeacherService teacherService;
    private final AttendanceService attendanceService;

    public TeacherController(UserService userService,
                             TeacherService teacherService,
                             AttendanceService attendanceService) {
        this.userService = userService;
        this.teacherService = teacherService;
        this.attendanceService = attendanceService;
    }

    @GetMapping("/home")
    public ModelAndView home(@AuthenticationPrincipal UserData userData) {
        var user = userService.getById(userData.getId());
        var viewData = teacherService.buildHomeView(user);
        
        long totalStudents = userService.getTotalStudentsCount();
        long activeClasses = userService.getActiveClassesCount();
        long postedAssignments = userService.getPostedAssignmentsCount();
        
        ModelAndView modelAndView = new ModelAndView("teacher/home");
        modelAndView.addObject("user", user);
        modelAndView.addObject("assignments", viewData.getAssignments());
        modelAndView.addObject("totalStudents", totalStudents);
        modelAndView.addObject("activeClasses", activeClasses);
        modelAndView.addObject("postedAssignments", postedAssignments);
        return modelAndView;
    }

    @GetMapping("/students")
    public ModelAndView students(@AuthenticationPrincipal UserData userData,
                                 @RequestParam(required = false) UUID courseId,
                                 @RequestParam(required = false) UUID studentId,
                                 @RequestParam(required = false) String successMessage,
                                 @RequestParam(required = false) String errorMessage) {
        User teacher = userService.getById(userData.getId());
        TeacherStudentsViewDto viewData = teacherService.buildStudentsView(teacher, courseId, studentId);
        ModelAndView modelAndView = new ModelAndView("teacher/students");
        modelAndView.addObject("user", teacher);
        modelAndView.addObject("courses", viewData.getCourses());
        modelAndView.addObject("selectedCourseId", viewData.getSelectedCourseId());
        modelAndView.addObject("students", viewData.getStudents());

        if (successMessage != null) {
            modelAndView.addObject("successMessage", successMessage);
        }
        String resolvedErrorMessage = errorMessage != null ? errorMessage : viewData.getGradeErrorMessage();
        if (resolvedErrorMessage != null) {
            modelAndView.addObject("errorMessage", resolvedErrorMessage);
        }

        modelAndView.addObject("selectedStudentId", viewData.getSelectedStudentId());
        modelAndView.addObject("selectedStudent", viewData.getSelectedStudent());
        modelAndView.addObject("studentGrades", viewData.getStudentGrades());
        modelAndView.addObject("showGradesModal", viewData.isShowGradesModal());
        modelAndView.addObject("assignments", viewData.getAssignments());
        return modelAndView;
    }

    @GetMapping("/schedule")
    public String schedule() {
        return "redirect:/teacher/home";
    }

    @GetMapping("/attendance")
    public ModelAndView attendance(@AuthenticationPrincipal UserData userData,
                                  @RequestParam(required = false) UUID courseId,
                                  @RequestParam(required = false) UUID studentId) {
        User teacher = userService.getById(userData.getId());
        var viewData = attendanceService.buildAttendanceView(teacher, courseId, studentId);
        
        ModelAndView modelAndView = new ModelAndView("teacher/attendance");
        modelAndView.addObject("user", teacher);
        modelAndView.addObject("courses", viewData.getCourses());
        modelAndView.addObject("selectedCourse", viewData.getSelectedCourse());
        modelAndView.addObject("students", viewData.getStudents());
        modelAndView.addObject("selectedStudentId", viewData.getSelectedStudentId());
        modelAndView.addObject("selectedStudent", viewData.getSelectedStudent());
        modelAndView.addObject("attendanceRecords", viewData.getAttendanceRecords());
        return modelAndView;
    }

    @PostMapping("/attendance")
    public ModelAndView markAttendance(@AuthenticationPrincipal UserData userData,
                                      @RequestParam UUID studentId,
                                      @RequestParam UUID courseId,
                                      @RequestParam String status,
                                      RedirectAttributes redirectAttributes) {
        attendanceService.markAttendance(userData.getId(), studentId, courseId, status);
        redirectAttributes.addFlashAttribute("successMessage", "Attendance marked successfully!");
        return new ModelAndView("redirect:/teacher/attendance?courseId=" + courseId);
    }

    @PostMapping("/attendance/delete")
    public ModelAndView deleteAttendance(@AuthenticationPrincipal UserData userData,
                                         @RequestParam String attendanceId,
                                         @RequestParam UUID courseId,
                                         RedirectAttributes redirectAttributes) {
        UUID attendanceUuid = UUID.fromString(attendanceId);
        attendanceService.deleteAttendanceRecord(userData.getId(), attendanceUuid);
        redirectAttributes.addFlashAttribute("successMessage", "Attendance record deleted successfully!");
        return new ModelAndView("redirect:/teacher/attendance?courseId=" + courseId);
    }

    @GetMapping("/edit-profile")
    public ModelAndView editProfile(@AuthenticationPrincipal UserData userData) {
        User user = userService.getById(userData.getId());
        EditProfileDto editProfileDto = userService.buildEditProfileDto(userData.getId());
        
        ModelAndView modelAndView = new ModelAndView("teacher/edit-profile");
        modelAndView.addObject("user", user);
        modelAndView.addObject("editProfileDto", editProfileDto);
        return modelAndView;
    }

    @PostMapping("/edit-profile")
    public ModelAndView updateProfile(@AuthenticationPrincipal UserData userData,
                                      @Valid @ModelAttribute EditProfileDto editProfileDto,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            User user = userService.getById(userData.getId());
            ModelAndView modelAndView = new ModelAndView("teacher/edit-profile");
            modelAndView.addObject("user", user);
            modelAndView.addObject("editProfileDto", editProfileDto);
            return modelAndView;
        }

        userService.updateUserProfile(userData.getId(), editProfileDto);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        return new ModelAndView("redirect:/teacher/home");
    }
}
