package com.example.OnlineNotebook.util;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.util.HashMap;
import java.util.Map;

public class ResponseHelper {

    private ResponseHelper() {
    }

    public static Map<String, Object> successResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        return response;
    }

    public static Map<String, Object> errorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }

    public static Map<String, Object> validationErrorResponse(BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Validation errors occurred");
        response.put("errors", bindingResult.getAllErrors());
        return response;
    }

    public static ResponseEntity<Map<String, Object>> ok(Map<String, Object> response) {
        return ResponseEntity.ok(response);
    }

    public static ResponseEntity<Map<String, Object>> badRequest(Map<String, Object> response) {
        return ResponseEntity.badRequest().body(response);
    }

    public static ResponseEntity<Map<String, Object>> buildResponse(boolean success, String message) {
        Map<String, Object> response = success 
            ? successResponse(message) 
            : errorResponse(message);
        
        return success ? ok(response) : badRequest(response);
    }
}

