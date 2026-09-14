package com.projectmanagement.app.ai;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiPmResponse {
    private String summary;
    private List<String> risks;
    private List<String> recommendations;
    private List<String> nextActions;
}
