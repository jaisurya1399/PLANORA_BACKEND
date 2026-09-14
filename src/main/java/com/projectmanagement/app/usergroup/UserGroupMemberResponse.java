package com.projectmanagement.app.usergroup;

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
public class UserGroupMemberResponse {
    private Long id;
    private String name;
    private String email;
    private boolean hasProfileImage;
}
