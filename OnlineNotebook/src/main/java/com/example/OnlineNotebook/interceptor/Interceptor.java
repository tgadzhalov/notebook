package com.example.OnlineNotebook.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class Interceptor implements HandlerInterceptor {

    private static final Set<String> OPEN_PATHS = Set.of("/", "/login", "/register", "/error"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();

        if (OPEN_PATHS.contains(path)) {
            return true;
        }
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user_id") == null) {
            String contextPath = request.getContextPath();
            response.sendRedirect(contextPath + "/login");
            return false;
        }

        return true;
    }
}
