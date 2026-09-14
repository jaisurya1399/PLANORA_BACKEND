package com.projectmanagement.app.auth;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationEntryPoint
                implements AuthenticationEntryPoint {

        private final ObjectMapper objectMapper;

        public JwtAuthenticationEntryPoint(
                        ObjectMapper objectMapper) {
                this.objectMapper = objectMapper;
        }

        @Override
        public void commence(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {

                response.setStatus(
                                HttpServletResponse.SC_UNAUTHORIZED);

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
                                HttpServletResponse.SC_UNAUTHORIZED);

                body.put(
                                "error",
                                "Unauthorized");

                body.put(
                                "message",
                                "Authentication is required");
                response.getWriter().write(
                                objectMapper.writeValueAsString(body));
        }
}
