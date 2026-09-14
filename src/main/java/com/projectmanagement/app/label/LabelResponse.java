package com.projectmanagement.app.label;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabelResponse {

    private Long id;

    private String name;

    private String color;

    private String description;

    private Long projectId;

    private String projectName;

    private Long ticketCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}