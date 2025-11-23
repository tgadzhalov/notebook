package com.example.OnlineNotebook.controller.teacher;

import com.example.OnlineNotebook.models.dtos.teacher.assignment.AssignmentFormDto;
import com.example.OnlineNotebook.models.entities.User;
import com.example.OnlineNotebook.security.UserData;
import com.example.OnlineNotebook.services.TeacherService;
import com.example.OnlineNotebook.services.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
public class TeacherAssignmentController {

    private final UserService userService;
    private final TeacherService teacherService;

    public TeacherAssignmentController(UserService userService, TeacherService teacherService) {
        this.userService = userService;
        this.teacherService = teacherService;
    }

    @GetMapping("/teacher/assignments")
    public ModelAndView assignments(@AuthenticationPrincipal UserData userData,
                                    @RequestParam(required = false) String successMessage,
                                    @RequestParam(required = false) String errorMessage) {
        User teacher = userService.getById(userData.getId());
        var viewData = teacherService.buildAssignmentsView(teacher);
        ModelAndView modelAndView = new ModelAndView("teacher/assignments");
        modelAndView.addObject("user", teacher);
        modelAndView.addObject("assignmentForm", viewData.getForm());
        modelAndView.addObject("courses", viewData.getCourses());
        modelAndView.addObject("assignmentTypes", viewData.getAssignmentTypes());
        if (successMessage != null) {
            modelAndView.addObject("successMessage", successMessage);
        }
        if (errorMessage != null) {
            modelAndView.addObject("errorMessage", errorMessage);
        }
        return modelAndView;
    }

    @PostMapping("/teacher/assignments")
    public String createAssignment(@AuthenticationPrincipal UserData userData,
                                   @Valid AssignmentFormDto assignmentFormDto,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        User teacher = userService.getById(userData.getId());

        if (bindingResult.hasErrors()) {
            var viewData = teacherService.buildAssignmentsView(teacher);
            model.addAttribute("user", teacher);
            model.addAttribute("assignmentForm", assignmentFormDto);
            model.addAttribute("courses", viewData.getCourses());
            model.addAttribute("assignmentTypes", viewData.getAssignmentTypes());
            model.addAttribute("errors", bindingResult);
            return "teacher/assignments";
        }

        teacherService.createAssignment(assignmentFormDto, teacher);
        redirectAttributes.addAttribute("successMessage", "Assignment published successfully.");
        return "redirect:/teacher/assignments";
    }

    @PostMapping("/teacher/assignments/{assignmentId}/delete")
    public String deleteAssignment(@AuthenticationPrincipal UserData userData,
                                   @PathVariable UUID assignmentId,
                                   RedirectAttributes redirectAttributes) {
        User teacher = userService.getById(userData.getId());
        teacherService.deleteAssignment(assignmentId, teacher);
        redirectAttributes.addAttribute("successMessage", "Assignment deleted.");
        return "redirect:/teacher/home";
    }
}
