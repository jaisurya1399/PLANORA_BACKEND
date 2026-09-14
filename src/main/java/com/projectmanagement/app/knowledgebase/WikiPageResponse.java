package com.projectmanagement.app.knowledgebase;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class WikiPageResponse {
    Long id;
    Long projectId;
    Long parentId;
    String title;
    String content;
    String updatedBy;
    int version;
    LocalDateTime updatedAt;
}
