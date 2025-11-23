package com.example.OnlineNotebook.controller;

import com.example.OnlineNotebook.models.dtos.auth.LoginDto;
import com.example.OnlineNotebook.models.dtos.auth.RegisterDto;
import com.example.OnlineNotebook.services.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    @GetMapping("/login")
    public ModelAndView getLogin() {
        ModelAndView modelAndView = new ModelAndView("login");
        modelAndView.addObject("loginDto", new LoginDto());
        return modelAndView;
    }

    @GetMapping("/register")
    public ModelAndView getRegister() {
        ModelAndView modelAndView = new ModelAndView("register");
        modelAndView.addObject("registerDto", new RegisterDto());
        return modelAndView;
    }

    @PostMapping("/register")
    public ModelAndView postRegister(
            @Valid RegisterDto registerDto, 
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("register");
            modelAndView.addObject("registerDto", registerDto);
            return modelAndView;
        }
        
        userService.registerUser(registerDto);
        return new ModelAndView("redirect:/login");
    }
}