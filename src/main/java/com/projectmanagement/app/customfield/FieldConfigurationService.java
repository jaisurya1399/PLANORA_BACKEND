package com.projectmanagement.app.customfield;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
public class FieldConfigurationService {
  private final FieldConfigurationRepository repo;
  private final CustomFieldRepository fieldRepo;
  private final ProjectRepository projectRepo;
  private final TicketTypeRepository typeRepo;
  private final ProjectAccessService access;

  @Transactional(readOnly = true)
  public List<FieldConfigurationResponse> all() {
    return repo.findAllByOrderByDisplayOrderAscIdAsc().stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public List<FieldConfigurationResponse> effective(Long projectId, Long typeId) {
    Project p = projectRepo.findById(projectId).orElseThrow(() -> nf("Project not found"));
    access.requireView(p);
    List<FieldConfiguration> rows = new ArrayList<>();
    rows.addAll(repo.findByProjectIdIsNullAndTicketTypeIdIsNullOrderByDisplayOrderAscIdAsc());
    rows.addAll(repo.findByProjectIdIsNullAndTicketTypeIdOrderByDisplayOrderAscIdAsc(typeId));
    rows.addAll(repo.findByProjectIdAndTicketTypeIdIsNullOrderByDisplayOrderAscIdAsc(projectId));
    rows.addAll(repo.findByProjectIdAndTicketTypeIdOrderByDisplayOrderAscIdAsc(projectId, typeId));
    Map<Long, FieldConfiguration> byField = new LinkedHashMap<>();
    for (FieldConfiguration r : rows)
      if (Boolean.TRUE.equals(r.getField().getActive()))
        byField.put(r.getField().getId(), r);
    for (CustomField f : fieldRepo.findByActiveTrueOrderByNameAsc())
      if (!byField.containsKey(f.getId()))
        byField.put(f.getId(), FieldConfiguration.builder().field(f).visible(true)
            .required(Boolean.TRUE.equals(f.getRequiredByDefault())).displayOrder(10000).build());
    return byField.values().stream()
        .sorted(Comparator.comparing(FieldConfiguration::getDisplayOrder).thenComparing(r -> r.getField().getName()))
        .map(this::toResponse).toList();
  }

  public FieldConfigurationResponse create(FieldConfigurationRequest r) {
    validateScope(r.getProjectId(), r.getTicketTypeId());
    CustomField f = fieldRepo.findById(r.getFieldId()).orElseThrow(() -> nf("Custom field not found"));
    Project p = r.getProjectId() == null ? null
        : projectRepo.findById(r.getProjectId()).orElseThrow(() -> nf("Project not found"));
    TicketType t = r.getTicketTypeId() == null ? null
        : typeRepo.findById(r.getTicketTypeId()).orElseThrow(() -> nf("Ticket type not found"));
    FieldConfiguration x = FieldConfiguration.builder().field(f).project(p).ticketType(t)
        .visible(r.getVisible() == null || r.getVisible()).required(Boolean.TRUE.equals(r.getRequired()))
        .displayOrder(r.getDisplayOrder() == null ? 0 : r.getDisplayOrder()).build();
    return toResponse(repo.save(x));
  }

  public FieldConfigurationResponse update(Long id, FieldConfigurationRequest r) {
    FieldConfiguration x = repo.findById(id).orElseThrow(() -> nf("Field configuration not found"));
    validateScope(r.getProjectId(), r.getTicketTypeId());
    x.setVisible(r.getVisible() == null || r.getVisible());
    x.setRequired(Boolean.TRUE.equals(r.getRequired()));
    x.setDisplayOrder(r.getDisplayOrder() == null ? 0 : r.getDisplayOrder());
    x.setProject(r.getProjectId() == null ? null
        : projectRepo.findById(r.getProjectId()).orElseThrow(() -> nf("Project not found")));
    x.setTicketType(r.getTicketTypeId() == null ? null
        : typeRepo.findById(r.getTicketTypeId()).orElseThrow(() -> nf("Ticket type not found")));
    return toResponse(repo.save(x));
  }

  public void delete(Long id) {
    repo.delete(repo.findById(id).orElseThrow(() -> nf("Field configuration not found")));
  }

  private void validateScope(Long projectId, Long typeId) {
    if (projectId != null)
      access.requireManager(projectRepo.findById(projectId).orElseThrow(() -> nf("Project not found")));
    if (typeId != null)
      typeRepo.findById(typeId).orElseThrow(() -> nf("Ticket type not found"));
  }

  private ResponseStatusException nf(String m) {
    return new ResponseStatusException(HttpStatus.NOT_FOUND, m);
  }

  private FieldConfigurationResponse toResponse(FieldConfiguration x) {
    return FieldConfigurationResponse.builder().id(x.getId()).fieldId(x.getField().getId())
        .fieldKey(x.getField().getKey()).fieldName(x.getField().getName()).fieldType(x.getField().getType())
        .optionsJson(x.getField().getOptionsJson()).projectId(x.getProject() == null ? null : x.getProject().getId())
        .ticketTypeId(x.getTicketType() == null ? null : x.getTicketType().getId()).visible(x.getVisible())
        .required(x.getRequired()).displayOrder(x.getDisplayOrder()).build();
  }
}
