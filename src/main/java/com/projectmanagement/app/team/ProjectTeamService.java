package com.projectmanagement.app.team;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectAccessService;
import com.projectmanagement.app.project.ProjectRepository;
import com.projectmanagement.app.user.User;
import com.projectmanagement.app.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectTeamService {
    private final ProjectTeamRepository teams;
    private final ProjectTeamMemberRepository members;
    private final ProjectRepository projects;
    private final UserRepository users;
    private final ProjectAccessService access;

    @Transactional(readOnly = true)
    public List<ProjectTeamResponse> list(Long pid) {
        Project p = projects.findById(pid).orElseThrow(() -> nf("Project not found"));
        access.requireView(p);
        return teams.findByProjectIdAndActiveTrueOrderByNameAsc(pid).stream().map(this::out).toList();
    }

    public ProjectTeamResponse create(ProjectTeamRequest r) {
        Project p = projects.findById(r.projectId).orElseThrow(() -> nf("Project not found"));
        access.requireManager(p);
        return out(teams.save(ProjectTeam.builder().project(p).name(r.name.trim()).description(r.description)
                .active(r.active == null || r.active).build()));
    }

    public ProjectTeamResponse update(Long id, ProjectTeamRequest r) {
        ProjectTeam t = teams.findById(id).orElseThrow(() -> nf("Team not found"));
        access.requireManager(t.project);
        t.setName(r.name.trim());
        t.setDescription(r.description);
        t.setActive(r.active == null || r.active);
        return out(teams.save(t));
    }

    public void delete(Long id) {
        ProjectTeam t = teams.findById(id).orElseThrow(() -> nf("Team not found"));
        access.requireManager(t.project);
        t.setActive(false);
    }

    public ProjectTeamResponse add(Long id, ProjectTeamMemberRequest r) {
        ProjectTeam t = teams.findById(id).orElseThrow(() -> nf("Team not found"));
        access.requireManager(t.project);
        User u = users.findById(r.userId).orElseThrow(() -> nf("User not found"));
        if (members.findByTeamIdAndUserId(id, u.getId()).isEmpty())
            members.save(ProjectTeamMember.builder().team(t).user(u).build());
        return out(t);
    }

    public ProjectTeamResponse remove(Long id, Long uid) {
        ProjectTeam t = teams.findById(id).orElseThrow(() -> nf("Team not found"));
        access.requireManager(t.project);
        members.findByTeamIdAndUserId(id, uid).ifPresent(members::delete);
        return out(t);
    }

    private ProjectTeamResponse out(ProjectTeam t) {
        var ms = members.findByTeamId(t.getId());
        return ProjectTeamResponse.builder().id(t.getId()).projectId(t.project.getId()).projectName(t.project.getName())
                .name(t.name).description(t.description).active(t.active)
                .memberIds(ms.stream().map(m -> m.user.getId()).toList())
                .memberNames(ms.stream().map(m -> m.user.getName()).toList()).build();
    }

    private ResponseStatusException nf(String m) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, m);
    }
}
