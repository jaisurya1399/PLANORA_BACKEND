package com.projectmanagement.app.knowledgebase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.auth.CurrentUserService;
import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectAccessService;
import com.projectmanagement.app.project.ProjectRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class WikiPageService {
    private final WikiPageRepository repo;
    private final ProjectRepository projects;
    private final ProjectAccessService access;
    private final CurrentUserService current;

    @Transactional(readOnly = true)
    public List<WikiPageResponse> list(Long pid) {
        Project p = projects.findById(pid).orElseThrow(() -> new RuntimeException("Project not found"));
        access.requireView(p);
        return repo.findByProjectIdOrderByTitleAsc(pid).stream().map(this::r).toList();
    }

    public WikiPageResponse save(Long id, WikiPageRequest x) {
        Project p = projects.findById(x.getProjectId()).orElseThrow(() -> new RuntimeException("Project not found"));
        access.requireEditor(p);
        WikiPage w = id == null ? new WikiPage()
                : repo.findById(id).orElseThrow(() -> new RuntimeException("Wiki page not found"));
        if (id != null && w.getProject().getId().longValue() != p.getId().longValue())
            throw new RuntimeException("Wrong project");
        w.setProject(p);
        w.setTitle(x.getTitle());
        w.setContent(x.getContent());
        w.setUpdatedBy(current.getCurrentUser());
        w.setVersion(id == null ? 1 : w.getVersion() + 1);
        if (x.getParentId() != null)
            w.setParent(
                    repo.findById(x.getParentId()).orElseThrow(() -> new RuntimeException("Parent page not found")));
        else
            w.setParent(null);
        return r(repo.save(w));
    }

    public void delete(Long id) {
        WikiPage w = repo.findById(id).orElseThrow(() -> new RuntimeException("Wiki page not found"));
        access.requireManager(w.getProject());
        repo.delete(w);
    }

    private WikiPageResponse r(WikiPage w) {
        return WikiPageResponse.builder().id(w.getId()).projectId(w.getProject().getId())
                .parentId(w.getParent() == null ? null : w.getParent().getId()).title(w.getTitle())
                .content(w.getContent()).updatedBy(w.getUpdatedBy() == null ? null : w.getUpdatedBy().getName())
                .version(w.getVersion()).updatedAt(w.getUpdatedAt()).build();
    }
}
