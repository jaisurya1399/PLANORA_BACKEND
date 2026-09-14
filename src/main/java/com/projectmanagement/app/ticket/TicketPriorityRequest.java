
package com.projectmanagement.app.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class TicketPriorityRequest {

    @NotBlank(message = "Priority name is required")
    @Size(max = 255, message = "Priority name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "Color is required")
    @Size(max = 255, message = "Color must not exceed 255 characters")
    @Builder.Default
    private String color = "#cecece";

    @Builder.Default
    private Boolean isDefault = false;

    @Builder.Default
    private Integer displayOrder = 0;
}