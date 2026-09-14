package com.projectmanagement.app.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class TicketStatusRequest {

    @NotBlank(message = "Status name is required")
    @Size(max = 255, message = "Status name must not exceed 255 characters")
    private String name;

    @NotBlank(message = "Color is required")
    @Size(max = 255, message = "Color must not exceed 255 characters")
    @Builder.Default
    private String color = "#cecece";

    @Builder.Default
    private Boolean isDefault = false;

    @Builder.Default
    @NotNull(message = "Order is required")
    private Integer order = 1;

    @NotNull(message = "Status category is required")
    @Builder.Default
    private TicketStatusCategory category = TicketStatusCategory.TODO;

    @Positive(message = "Project ID must be positive")
    private Long projectId;
}
