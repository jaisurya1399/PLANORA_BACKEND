package com.projectmanagement.app.dependency;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DependencyResponse {
    Long id;
    Long sourceTicketId;
    String sourceCode;
    Long targetTicketId;
    String targetCode;
    String type;
    String description;
}
