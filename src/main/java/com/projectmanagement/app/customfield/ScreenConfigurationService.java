package com.projectmanagement.app.customfield;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.projectmanagement.app.project.Project;
import com.projectmanagement.app.project.ProjectAccessService;
import com.projectmanagement.app.project.ProjectRepository;
import com.projectmanagement.app.ticket.TicketType;
import com.projectmanagement.app.ticket.TicketTypeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ScreenConfigurationService {
    private final ScreenConfigurationRepository repo;
    private final ScreenFieldRepository fieldRepo;
    private final CustomFieldRepository customFieldRepo;
    private final ProjectRepository projectRepo;
    private final TicketTypeRepository typeRepo;
    private final ProjectAccessService access;

    @Transactional(readOnly = true)
    public List<ScreenConfigurationResponse> all() {
        return repo.findAllByOrderByNameAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ScreenConfigurationResponse> effective(Long projectId, Long typeId) {
        Project p = projectRepo.findById(projectId).orElseThrow(() -> nf("Project not found"));
        access.requireView(p);
        List<ScreenConfiguration> screens = new ArrayList<>();
        screens.addAll(repo.findByProjectIdIsNullAndTicketTypeIdIsNullAndActiveTrue());
        screens.addAll(repo.findByProjectIdIsNullAndTicketTypeIdAndActiveTrue(typeId));
        screens.addAll(repo.findByProjectIdAndTicketTypeIdIsNullAndActiveTrue(projectId));
        screens.addAll(repo.findByProjectIdAndTicketTypeIdAndActiveTrue(projectId, typeId));
        return screens.isEmpty() ? List.of()
                : List.of(screens.get(screens.size() - 1)).stream().map(this::toResponse).toList();
    }

    public ScreenConfigurationResponse create(ScreenConfigurationRequest r) {
        Project p = r.getProjectId() == null ? null
                : projectRepo.findById(r.getProjectId()).orElseThrow(() -> nf("Project not found"));
        if (p != null)
            access.requireManager(p);
        TicketType t = r.getTicketTypeId() == null ? null
                : typeRepo.findById(r.getTicketTypeId()).orElseThrow(() -> nf("Ticket type not found"));
        ScreenConfiguration s = ScreenConfiguration.builder().name(r.getName().trim()).project(p).ticketType(t)
                .active(r.getActive() == null || r.getActive()).build();
        return toResponse(repo.save(s));
    }

    public ScreenConfigurationResponse update(Long id, ScreenConfigurationRequest r) {
        ScreenConfiguration s = repo.findById(id).orElseThrow(() -> nf("Screen not found"));
        Project p = r.getProjectId() == null ? null
                : projectRepo.findById(r.getProjectId()).orElseThrow(() -> nf("Project not found"));
        if (p != null)
            access.requireManager(p);
        s.setName(r.getName().trim());
        s.setProject(p);
        s.setTicketType(r.getTicketTypeId() == null ? null
                : typeRepo.findById(r.getTicketTypeId()).orElseThrow(() -> nf("Ticket type not found")));
        s.setActive(r.getActive() == null || r.getActive());
        return toResponse(repo.save(s));
    }

    public void delete(Long id) {
        ScreenConfiguration s = repo.findById(id).orElseThrow(() -> nf("Screen not found"));
        fieldRepo.findByScreenIdOrderByDisplayOrderAscIdAsc(id).forEach(fieldRepo::delete);
        repo.delete(s);
    }

    public ScreenConfigurationResponse addField(Long id, ScreenFieldRequest r) {
        ScreenConfiguration s = repo.findById(id).orElseThrow(() -> nf("Screen not found"));
        if (s.getProject() != null)
            access.requireManager(s.getProject());
        CustomField f = customFieldRepo.findById(r.getFieldId()).orElseThrow(() -> nf("Custom field not found"));
        if (fieldRepo.existsByScreenIdAndFieldId(id, r.getFieldId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Field already exists on this screen");
        ScreenField sf = ScreenField.builder().screen(s).field(f)
                .displayOrder(r.getDisplayOrder() == null ? 0 : r.getDisplayOrder())
                .visible(r.getVisible() == null || r.getVisible()).build();
        fieldRepo.save(sf);
        return toResponse(s);
    }

    public void removeField(Long id, Long fieldId) {
        ScreenConfiguration s = repo.findById(id).orElseThrow(() -> nf("Screen not found"));
        if (s.getProject() != null)
            access.requireManager(s.getProject());
        fieldRepo.findByScreenIdOrderByDisplayOrderAscIdAsc(id).stream()
                .filter(x -> x.getField().getId().equals(fieldId)).findFirst().ifPresent(fieldRepo::delete);
    }

    public ScreenConfigurationResponse updateField(Long id, Long fieldId, ScreenFieldRequest r) {
        ScreenConfiguration s = repo.findById(id).orElseThrow(() -> nf("Screen not found"));
        if (s.getProject() != null)
            access.requireManager(s.getProject());
        ScreenField sf = fieldRepo.findByScreenIdOrderByDisplayOrderAscIdAsc(id).stream()
                .filter(x -> x.getField().getId().equals(fieldId)).findFirst()
                .orElseThrow(() -> nf("Screen field not found"));
        sf.setDisplayOrder(r.getDisplayOrder() == null ? sf.getDisplayOrder() : r.getDisplayOrder());
        sf.setVisible(r.getVisible() == null || r.getVisible());
        fieldRepo.save(sf);
        return toResponse(s);
    }

    private ScreenConfigurationResponse toResponse(ScreenConfiguration s) {
        return ScreenConfigurationResponse.builder().id(s.getId()).name(s.getName())
                .projectId(s.getProject() == null ? null : s.getProject().getId())
                .ticketTypeId(s.getTicketType() == null ? null : s.getTicketType().getId()).active(s.getActive())
                .fields(fieldRepo.findByScreenIdOrderByDisplayOrderAscIdAsc(s.getId()).stream()
                        .map(x -> ScreenFieldResponse.builder().id(x.getId()).fieldId(x.getField().getId())
                                .fieldKey(x.getField().getKey()).fieldName(x.getField().getName())
                                .fieldType(x.getField().getType()).optionsJson(x.getField().getOptionsJson())
                                .displayOrder(x.getDisplayOrder()).visible(x.getVisible()).build())
                        .toList())
                .build();
    }

    private ResponseStatusException nf(String m) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, m);
    }
}
