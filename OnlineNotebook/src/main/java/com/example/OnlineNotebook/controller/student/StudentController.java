package com.example.OnlineNotebook.controller.student;

import com.example.OnlineNotebook.models.dtos.auth.EditProfileDto;
import com.example.OnlineNotebook.models.dtos.student.grades.StudentGradesViewDto;
import com.example.OnlineNotebook.models.dtos.student.home.StudentHomeViewDto;
import com.example.OnlineNotebook.models.entities.User;
import com.example.OnlineNotebook.security.UserData;
import com.example.OnlineNotebook.services.StudentGradesService;
import com.example.OnlineNotebook.services.StudentService;
import com.example.OnlineNotebook.services.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final UserService userService;
    private final StudentService studentHomeService;
    private final StudentGradesService studentGradesService;

    public StudentController(UserService userService,
                             StudentService studentHomeService,
                             StudentGradesService studentGradesService) {
        this.userService = userService;
        this.studentHomeService = studentHomeService;
        this.studentGradesService = studentGradesService;
    }

    @GetMapping("/home")
    public ModelAndView home(@AuthenticationPrincipal UserData userData) {
        StudentHomeViewDto homeView = studentHomeService.buildHomeView(userData.getId());
        ModelAndView modelAndView = new ModelAndView("student/home");
        modelAndView.addObject("homeView", homeView);
        return modelAndView;
    }
    
    @GetMapping("/grades")
    public ModelAndView grades(@AuthenticationPrincipal UserData userData) {
        StudentGradesViewDto gradesView = studentGradesService.buildGradesView(userData.getId());
        ModelAndView modelAndView = new ModelAndView("student/grades");
        modelAndView.addObject("gradesView", gradesView);
        return modelAndView;
    }
    
    @GetMapping("/schedule")
    public String schedule() {
        return "student/schedule";
    }
    
    @GetMapping("/edit-profile")
    public ModelAndView editProfile(@AuthenticationPrincipal UserData userData) {
        User user = userService.getById(userData.getId());
        EditProfileDto editProfileDto = userService.buildEditProfileDto(userData.getId());
        
        ModelAndView modelAndView = new ModelAndView("student/edit-profile");
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
            ModelAndView modelAndView = new ModelAndView("student/edit-profile");
            modelAndView.addObject("user", user);
            modelAndView.addObject("editProfileDto", editProfileDto);
            return modelAndView;
        }

        userService.updateUserProfile(userData.getId(), editProfileDto);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        return new ModelAndView("redirect:/student/home");
    }
}