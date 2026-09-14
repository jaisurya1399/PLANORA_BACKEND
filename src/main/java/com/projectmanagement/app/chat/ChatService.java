package com.projectmanagement.app.chat;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.auth.CurrentUserService;
import com.projectmanagement.app.meeting.Meeting;
import com.projectmanagement.app.meeting.MeetingRepository;
import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectAccessService;
import com.projectmanagement.app.project.ProjectRepository;
import com.projectmanagement.app.realtime.RealtimeEventService;
import com.projectmanagement.app.user.User;
import com.projectmanagement.app.user.UserRepository;

@Service
@Transactional
public class ChatService {
    private final ChatMessageRepository repo;
    private final CurrentUserService current;
    private final ProjectRepository projects;
    private final ProjectAccessService access;
    private final RealtimeEventService events;
    private final UserRepository users;
    private final MeetingRepository meetings;

    public ChatService(ChatMessageRepository repo, CurrentUserService current, ProjectRepository projects,
            ProjectAccessService access, RealtimeEventService events, UserRepository users,
            MeetingRepository meetings) {
        this.repo = repo;
        this.current = current;
        this.projects = projects;
        this.access = access;
        this.events = events;
        this.users = users;
        this.meetings = meetings;
    }

    public List<ChatMessageResponse> direct(Long other) {
        Long me = current.getCurrentUserId();
        return repo.findTop100ByRoomTypeAndSenderIdAndRecipientIdOrRoomTypeAndSenderIdAndRecipientIdOrderByCreatedAtAsc(
                "DIRECT", me, other, "DIRECT", other, me).stream().map(this::to).collect(Collectors.toList());
    }

    public List<ChatMessageResponse> room(Long projectId) {
        Project p = projects.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
        access.requireView(p);
        return repo.findTop100ByRoomTypeAndRoomIdOrderByCreatedAtAsc("PROJECT", projectId).stream().map(this::to)
                .collect(Collectors.toList());
    }

    public List<ChatMessageResponse> meetingRoom(Long meetingId) {
        Meeting m = meetings.findById(meetingId).orElseThrow(() -> new RuntimeException("Meeting not found"));
        access.requireView(m.getProject());
        return repo.findTop100ByRoomTypeAndRoomIdOrderByCreatedAtAsc("MEETING", meetingId).stream().map(this::to)
                .collect(Collectors.toList());
    }

    public ChatMessageResponse sendDirect(Long recipient, ChatMessageRequest req) {
        if (recipient == null)
            throw new RuntimeException("Recipient is required");
        User target = users.findById(recipient).orElseThrow(() -> new RuntimeException("Recipient not found"));
        ChatMessage m = ChatMessage.builder().roomType("DIRECT").senderId(current.getCurrentUserId())
                .recipientId(target.getId()).content(req.getContent().trim()).build();
        ChatMessageResponse out = to(repo.save(m));
        events.publishUser(recipient, "chat-message", out);
        events.publishUser(current.getCurrentUserId(), "chat-message", out);
        return out;
    }

    public ChatMessageResponse sendRoom(Long projectId, ChatMessageRequest req) {
        Project p = projects.findById(projectId).orElseThrow(() -> new RuntimeException("Project not found"));
        access.requireEditor(p);
        ChatMessage m = ChatMessage.builder().roomType("PROJECT").roomId(projectId).senderId(current.getCurrentUserId())
                .content(req.getContent().trim()).build();
        ChatMessageResponse out = to(repo.save(m));
        events.publishProject(projectId, "chat-message", out);
        return out;
    }

    public ChatMessageResponse sendMeeting(Long meetingId, ChatMessageRequest req) {
        Meeting meeting = meetings.findById(meetingId).orElseThrow(() -> new RuntimeException("Meeting not found"));
        access.requireEditor(meeting.getProject());
        ChatMessage m = ChatMessage.builder().roomType("MEETING").roomId(meetingId).senderId(current.getCurrentUserId())
                .content(req.getContent().trim()).build();
        ChatMessageResponse out = to(repo.save(m));
        events.publishProject(meetingId, "meeting-chat-message", out);
        return out;
    }

    private ChatMessageResponse to(ChatMessage m) {
        User sender = users.findById(m.getSenderId()).orElse(null);
        User recipient = m.getRecipientId() == null ? null : users.findById(m.getRecipientId()).orElse(null);
        return ChatMessageResponse.builder().id(m.getId()).roomType(m.getRoomType()).roomId(m.getRoomId())
                .senderId(m.getSenderId()).senderName(sender == null ? "Unknown user" : sender.getName())
                .senderEmail(sender == null ? null : sender.getEmail()).recipientId(m.getRecipientId())
                .recipientName(recipient == null ? null : recipient.getName()).content(m.getContent())
                .createdAt(m.getCreatedAt()).build();
    }
}
