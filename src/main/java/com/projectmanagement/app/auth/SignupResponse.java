package com.projectmanagement.app.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SignupResponse {

    private Long userId;
    private String name;
    private String email;
    private String message;
}