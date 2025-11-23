package com.example.OnlineNotebook.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = ResourceNotFoundException.class)
    public ModelAndView handleNotFoundExceptions(ResourceNotFoundException e){
        ModelAndView modelAndView = new ModelAndView("error/resource-not-found");
        String message = e.getMessage();
        if (message != null && !message.isEmpty()) {
            modelAndView.addObject("message", message);
        }
        return modelAndView;
    }

    @ExceptionHandler(value = IllegalArgumentException.class)
    public Object handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        
        if (requestURI != null && requestURI.startsWith("/api/")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        
        ModelAndView modelAndView = new ModelAndView("error/error");
        String message = e.getMessage();
        if (message != null && !message.isEmpty()) {
            modelAndView.addObject("message", message);
        }
        return modelAndView;
    }

    @ExceptionHandler(value = Exception.class)
    public ModelAndView handleAllOtherExceptions(Exception e){
        ModelAndView modelAndView = new ModelAndView("error/error");
        String message = e.getMessage();
        if (message != null && !message.isEmpty()) {
            modelAndView.addObject("message", message);
        }
        return modelAndView;
    }
}
