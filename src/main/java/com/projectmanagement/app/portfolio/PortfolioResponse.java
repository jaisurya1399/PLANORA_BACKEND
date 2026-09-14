package com.projectmanagement.app.portfolio;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PortfolioResponse {
    Long id;
    String name;
    String description;
    List<PortfolioProject> projects;
}
