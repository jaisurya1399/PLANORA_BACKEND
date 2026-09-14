package com.projectmanagement.app.board;

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
public class BoardConfigResponse {
    private Long id;
    private Long projectId;
    private BoardSwimlaneType swimlaneType;
    private Boolean enforceWip;
    private Boolean activeSprintOnly;
    private Boolean showEpic;
}
