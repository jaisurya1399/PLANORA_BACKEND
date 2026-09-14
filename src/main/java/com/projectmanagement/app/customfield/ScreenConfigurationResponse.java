package com.projectmanagement.app.customfield;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScreenConfigurationResponse {
    private Long id;
    private String name;
    private Long projectId;
    private Long ticketTypeId;
    private Boolean active;
    private List<ScreenFieldResponse> fields;
}
