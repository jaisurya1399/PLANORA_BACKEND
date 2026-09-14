package com.projectmanagement.app.dailyscrum;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectRepository;
import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor @Transactional
public class ScrumMeetingService {
    private final ScrumMeetingRepository repository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly=true)
    public List<ScrumMeetingResponse> getAll(){ return repository.findAll().stream().map(this::toResponse).toList(); }
    @Transactional(readOnly=true)
    public ScrumMeetingResponse getByProject(Long projectId){
        return repository.findByProjectId(projectId).map(this::toResponse).orElse(null);
    }
    public ScrumMeetingResponse save(ScrumMeetingRequest request){
        Project project=projectRepository.findById(request.getProjectId()).orElseThrow(()->new RuntimeException("Project not found"));
        ScrumMeeting meeting=repository.findByProjectId(project.getId()).orElseGet(ScrumMeeting::new);
        meeting.setProject(project); meeting.setMeetingTitle(request.getMeetingTitle().trim()); meeting.setMeetingUrl(request.getMeetingUrl().trim()); meeting.setMeetingTime(request.getMeetingTime().trim()); meeting.setActive(request.isActive());
        return toResponse(repository.save(meeting));
    }
    public void delete(Long id){ repository.deleteById(id); }
    private ScrumMeetingResponse toResponse(ScrumMeeting m){ return ScrumMeetingResponse.builder().id(m.getId()).projectId(m.getProject().getId()).projectName(m.getProject().getName()).meetingTitle(m.getMeetingTitle()).meetingUrl(m.getMeetingUrl()).meetingTime(m.getMeetingTime()).active(m.isActive()).createdAt(m.getCreatedAt()).updatedAt(m.getUpdatedAt()).build(); }
}
