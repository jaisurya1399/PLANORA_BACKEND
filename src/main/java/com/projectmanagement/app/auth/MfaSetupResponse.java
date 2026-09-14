package com.projectmanagement.app.auth;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MfaSetupResponse {
    private boolean enabled;
    private String secret;
    private String otpauthUrl;
    private List<String> recoveryCodes;
}
