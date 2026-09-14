package com.projectmanagement.app.setting;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SettingResponse {

    private Long id;

    private String group;

    private String name;

    private Boolean locked;

    private String payload;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}