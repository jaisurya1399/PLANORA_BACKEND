package com.projectmanagement.app.board;

import jakarta.validation.constraints.NotNull;
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
public class BoardConfigRequest {
    private BoardSwimlaneType swimlaneType;
    @NotNull
    private Boolean enforceWip;
    @NotNull
    private Boolean activeSprintOnly;
    @NotNull
    private Boolean showEpic;
}
