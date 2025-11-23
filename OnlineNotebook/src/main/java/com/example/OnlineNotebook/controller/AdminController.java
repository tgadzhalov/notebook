package com.example.OnlineNotebook.controller;

import com.example.OnlineNotebook.models.dtos.admin.BulkImportUserDto;
import com.example.OnlineNotebook.models.dtos.course.CourseDto;
import com.example.OnlineNotebook.models.dtos.auth.RegisterDto;
import com.example.OnlineNotebook.services.CourseService;
import com.example.OnlineNotebook.services.UserService;
import com.example.OnlineNotebook.util.ResponseHelper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final CourseService courseService;

    public AdminController(UserService userService, CourseService courseService) {
        this.userService = userService;
        this.courseService = courseService;
    }

    @GetMapping("/admin-panel")
    public ModelAndView adminPanel() {
        ModelAndView modelAndView = new ModelAndView("admin-panel");
        modelAndView.addObject("registerDto", new RegisterDto());
        modelAndView.addObject("courseDto", new CourseDto());
        return modelAndView;
    }

    @GetMapping("/api/v1/admin/users")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getRecentUsers(
            @RequestParam(defaultValue = "20") int limit) {
        List<Map<String, Object>> users = userService.getRecentUsers(limit);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/api/v1/admin/users")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createUser(
            @Valid @RequestBody RegisterDto registerDto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseHelper.badRequest(ResponseHelper.validationErrorResponse(bindingResult));
        }

        var result = userService.registerUserWithResult(registerDto);
        return ResponseHelper.buildResponse(result.isSuccess(), result.getMessage());
    }

    @PostMapping("/api/v1/admin/users/import")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createBulkUsers(
            @RequestBody List<BulkImportUserDto> users) {

        var result = userService.registerBulkUsers(users);
        return ResponseHelper.buildResponse(result.isSuccess(), result.getMessage());
    }

    @GetMapping("/api/v1/admin/teachers")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getTeachers() {
        List<Map<String, Object>> teachers = courseService.getTeachersList();
        return ResponseEntity.ok(teachers);
    }

    @PostMapping("/api/v1/admin/courses")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createCourse(
            @Valid @RequestBody CourseDto courseDto,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseHelper.badRequest(ResponseHelper.validationErrorResponse(bindingResult));
        }

        courseService.createCourse(courseDto);
        return ResponseHelper.ok(ResponseHelper.successResponse("Course created successfully"));
    }
}

