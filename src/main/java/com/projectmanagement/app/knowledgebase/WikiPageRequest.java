package com.projectmanagement.app.knowledgebase;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WikiPageRequest {
    Long projectId;
    Long parentId;
    String title;
    String content;
}
