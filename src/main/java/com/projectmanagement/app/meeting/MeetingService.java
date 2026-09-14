package com.projectmanagement.app.meeting;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.auth.CurrentUserService;
import com.projectmanagement.app.document.Document;
import com.projectmanagement.app.document.DocumentRepository;
import com.projectmanagement.app.epic.Epic;
import com.projectmanagement.app.epic.EpicRepository;
import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectAccessService;
import com.projectmanagement.app.project.ProjectRepository;
import com.projectmanagement.app.project.ProjectUserRepository;

@Service
@Transactional
public class MeetingService {
    private final MeetingRepository repo;
    private final MeetingAttendeeRepository attendees;
    private final MeetingDocumentRepository meetingDocs;
    private final ProjectRepository projects;
    private final ProjectUserRepository projectUsers;
    private final ProjectAccessService access;
    private final CurrentUserService current;
    private final EpicRepository epics;
    private final DocumentRepository documents;

    public MeetingService(MeetingRepository r, MeetingAttendeeRepository a, MeetingDocumentRepository md,
            ProjectRepository p, ProjectUserRepository pu, ProjectAccessService x, CurrentUserService c,
            EpicRepository e, DocumentRepository d) {
        repo = r;
        attendees = a;
        meetingDocs = md;
        projects = p;
        projectUsers = pu;
        access = x;
        current = c;
        epics = e;
        documents = d;
    }

    public List<MeetingResponse> list(Long pid) {
        Project p = projects.findById(pid).orElseThrow(() -> new RuntimeException("Project not found"));
        access.requireView(p);
        return repo.findByProjectIdOrderByStartsAtDesc(pid).stream().map(this::to).collect(Collectors.toList());
    }

    public MeetingResponse create(Long pid, MeetingRequest req) {
        Project p = projects.findById(pid).orElseThrow(() -> new RuntimeException("Project not found"));
        access.requireManager(p);
        String type = req.getMeetingType().trim().toUpperCase();
        if (!type.equals("ONLINE") && !type.equals("OFFLINE"))
            throw new RuntimeException("Meeting type must be ONLINE or OFFLINE");
        if (req.getEndsAt() != null && !req.getEndsAt().isAfter(req.getStartsAt()))
            throw new RuntimeException("Meeting end must be after start");
        Epic epic = null;
        if (req.getEpicId() != null) {
            epic = epics.findById(req.getEpicId()).orElseThrow(() -> new RuntimeException("Epic not found"));
            if (!epic.getProject().getId().equals(pid))
                throw new RuntimeException("Epic does not belong to this project");
        }
        if (type.equals("ONLINE") && (req.getMeetingUrl() == null || req.getMeetingUrl().isBlank()))
            throw new RuntimeException("Online meeting URL is required");
        if (type.equals("OFFLINE") && (req.getLocation() == null || req.getLocation().isBlank()))
            throw new RuntimeException("Offline meeting location is required");
        Meeting m = repo.save(Meeting.builder().project(p).epic(epic).title(req.getTitle().trim())
                .agenda(req.getAgenda()).startsAt(req.getStartsAt()).endsAt(req.getEndsAt()).meetingType(type)
                .meetingUrl(type.equals("ONLINE") ? req.getMeetingUrl() : null)
                .location(type.equals("OFFLINE") ? req.getLocation() : null).createdBy(current.getCurrentUserId())
                .status("SCHEDULED").inviteAllTeam(req.isInviteAllTeam()).build());
        Set<Long> ids = new LinkedHashSet<>();
        if (req.isInviteAllTeam())
            projectUsers.findByProjectId(pid).forEach(x -> ids.add(x.getUser().getId()));
        if (req.getAttendeeIds() != null)
            ids.addAll(req.getAttendeeIds());
        for (Long uid : ids) {
            if (!projectUsers.existsByProjectIdAndUserId(pid, uid))
                throw new RuntimeException("User " + uid + " is not a member of this project");
            attendees.save(MeetingAttendee.builder().meeting(m)
                    .user(projectUsers.findByProjectIdAndUserId(pid, uid).orElseThrow().getUser())
                    .responseStatus("INVITED").build());
        }
        return to(m);
    }

    public MeetingResponse updateStatus(Long id, String status) {
        Meeting m = getMeeting(id);
        access.requireManager(m.getProject());
        m.setStatus(status.trim().toUpperCase());
        return to(repo.save(m));
    }

    public Meeting getMeeting(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Meeting not found"));
    }

    public List<MeetingDocumentResponse> listDocuments(Long id) {
        Meeting m = getMeeting(id);
        access.requireView(m.getProject());
        return meetingDocs.findByMeetingId(id).stream()
                .map(x -> MeetingDocumentResponse.builder().id(x.getId()).documentId(x.getDocument().getId())
                        .name(x.getDocument().getName()).originalName(x.getDocument().getOriginalName())
                        .contentType(x.getDocument().getContentType()).fileSize(x.getDocument().getFileSize())
                        .createdAt(x.getDocument().getCreatedAt()).build())
                .toList();
    }

    public MeetingDocumentResponse attachDocument(Long id, Long documentId) {
        Meeting m = getMeeting(id);
        access.requireEditor(m.getProject());
        Document d = documents.findById(documentId).orElseThrow(() -> new RuntimeException("Document not found"));
        MeetingDocument x = meetingDocs.findByMeetingIdAndDocumentId(id, documentId)
                .orElseGet(() -> meetingDocs.save(MeetingDocument.builder().meeting(m).document(d).build()));
        return MeetingDocumentResponse.builder().id(x.getId()).documentId(d.getId()).name(d.getName())
                .originalName(d.getOriginalName()).contentType(d.getContentType()).fileSize(d.getFileSize())
                .createdAt(d.getCreatedAt()).build();
    }

    public void removeDocument(Long id, Long documentId) {
        Meeting m = getMeeting(id);
        access.requireEditor(m.getProject());
        meetingDocs.findByMeetingIdAndDocumentId(id, documentId).ifPresent(meetingDocs::delete);
    }

    private MeetingResponse to(Meeting m) {
        List<MeetingAttendeeResponse> as = attendees.findByMeetingId(m.getId()).stream()
                .map(a -> MeetingAttendeeResponse.builder().userId(a.getUser().getId()).name(a.getUser().getName())
                        .email(a.getUser().getEmail()).responseStatus(a.getResponseStatus()).build())
                .toList();
        return MeetingResponse.builder().id(m.getId()).projectId(m.getProject().getId())
                .projectName(m.getProject().getName()).epicId(m.getEpic() == null ? null : m.getEpic().getId())
                .epicName(m.getEpic() == null ? null : m.getEpic().getName()).createdBy(m.getCreatedBy())
                .title(m.getTitle()).agenda(m.getAgenda()).meetingType(m.getMeetingType()).meetingUrl(m.getMeetingUrl())
                .location(m.getLocation()).status(m.getStatus()).inviteAllTeam(m.isInviteAllTeam())
                .startsAt(m.getStartsAt()).endsAt(m.getEndsAt()).createdAt(m.getCreatedAt()).attendees(as).build();
    }
}
