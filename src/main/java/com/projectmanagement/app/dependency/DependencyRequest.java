package com.projectmanagement.app.dependency;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DependencyRequest {
    Long sourceTicketId;
    Long targetTicketId;
    String type;
    String description;
}
