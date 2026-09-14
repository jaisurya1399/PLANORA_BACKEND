package com.projectmanagement.app.board;

import jakarta.validation.constraints.Min;
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
public class BoardColumnRequest {
    @NotNull
    private Long statusId;
    private String displayName;
    @NotNull
    private Integer displayOrder;
    @NotNull
    private Boolean enabled;
    @Min(0)
    private Integer wipLimit;
}
