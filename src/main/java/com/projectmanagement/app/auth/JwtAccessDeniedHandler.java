package com.projectmanagement.app.auth;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAccessDeniedHandler
                implements AccessDeniedHandler {

        private final ObjectMapper objectMapper;

        public JwtAccessDeniedHandler(
                        ObjectMapper objectMapper) {
                this.objectMapper = objectMapper;
        }

        @Override
        public void handle(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        AccessDeniedException accessDeniedException) throws IOException, ServletException {

                response.setStatus(
                                HttpServletResponse.SC_FORBIDDEN);

                response.setContentType(
                                "application/json");

                response.setCharacterEncoding(
                                "UTF-8");

                Map<String, Object> body = new LinkedHashMap<>();

                body.put(
                                "timestamp",
                                LocalDateTime.now());

                body.put(
                                "status",
                                HttpServletResponse.SC_FORBIDDEN);

                body.put(
                                "error",
                                "Forbidden");

                body.put(
                                "message",
                                "You do not have permission to access this resource");
                response.getWriter().write(
                                objectMapper.writeValueAsString(body));
        }
}
