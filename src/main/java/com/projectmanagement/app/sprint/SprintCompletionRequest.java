package com.projectmanagement.app.sprint;

import jakarta.validation.constraints.Positive;
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
public class SprintCompletionRequest {
    /** If supplied, incomplete issues are moved to this planned/active sprint. */
    @Positive
    private Long carryOverSprintId;
    /**
     * Default true: incomplete issues are returned to the project backlog when no
     * target sprint is supplied.
     */
    @Builder.Default
    private Boolean moveIncompleteToBacklog = true;
}
