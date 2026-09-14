package com.projectmanagement.app.usergroup;

import java.time.LocalDateTime;
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
public class UserGroupResponse {
    private Long id;
    private String name;
    private String description;
    private List<UserGroupMemberResponse> members;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
