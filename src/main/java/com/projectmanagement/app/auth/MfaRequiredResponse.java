package com.projectmanagement.app.auth;

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
public class MfaRequiredResponse {
    private boolean mfaRequired;
    private String mfaToken;
    private String email;
}
