package com.projectmanagement.app.label;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class LabelController {

    private final LabelService labelService;

    // =========================================================
    // PROJECT LABELS
    // =========================================================

    @PostMapping("/projects/{projectId}/labels")
    public ResponseEntity<LabelResponse> createLabel(
            @PathVariable Long projectId,
            @Valid @RequestBody LabelRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        labelService.create(
                                projectId,
                                request));
    }

    @GetMapping("/projects/{projectId}/labels")
    public ResponseEntity<List<LabelResponse>> getLabels(
            @PathVariable Long projectId) {

        return ResponseEntity.ok(
                labelService.getProjectLabels(
                        projectId));
    }

    @GetMapping("/projects/{projectId}/labels/{labelId}")
    public ResponseEntity<LabelResponse> getLabel(
            @PathVariable Long projectId,
            @PathVariable Long labelId) {

        return ResponseEntity.ok(
                labelService.get(
                        projectId,
                        labelId));
    }

    @PutMapping("/projects/{projectId}/labels/{labelId}")
    public ResponseEntity<LabelResponse> updateLabel(
            @PathVariable Long projectId,
            @PathVariable Long labelId,
            @Valid @RequestBody LabelRequest request) {

        return ResponseEntity.ok(
                labelService.update(
                        projectId,
                        labelId,
                        request));
    }

    @DeleteMapping("/projects/{projectId}/labels/{labelId}")
    public ResponseEntity<Void> deleteLabel(
            @PathVariable Long projectId,
            @PathVariable Long labelId) {

        labelService.delete(
                projectId,
                labelId);

        return ResponseEntity.noContent().build();
    }

    // =========================================================
    // TICKET LABELS
    // =========================================================

    @PostMapping("/projects/{projectId}/tickets/{ticketId}/labels/{labelId}")
    public ResponseEntity<LabelResponse> addLabelToTicket(
            @PathVariable Long projectId,
            @PathVariable Long ticketId,
            @PathVariable Long labelId) {

        return ResponseEntity.ok(
                labelService.addLabelToTicket(
                        projectId,
                        ticketId,
                        labelId));
    }

    @DeleteMapping("/projects/{projectId}/tickets/{ticketId}/labels/{labelId}")
    public ResponseEntity<Void> removeLabelFromTicket(
            @PathVariable Long projectId,
            @PathVariable Long ticketId,
            @PathVariable Long labelId) {

        labelService.removeLabelFromTicket(
                projectId,
                ticketId,
                labelId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/projects/{projectId}/tickets/{ticketId}/labels")
    public ResponseEntity<List<LabelResponse>> getTicketLabels(
            @PathVariable Long projectId,
            @PathVariable Long ticketId) {

        return ResponseEntity.ok(
                labelService.getTicketLabels(
                        projectId,
                        ticketId));
    }

    @GetMapping("/projects/{projectId}/labels/{labelId}/tickets")
    public ResponseEntity<?> getLabelTickets(
            @PathVariable Long projectId,
            @PathVariable Long labelId) {

        return ResponseEntity.ok(
                labelService.getTicketsByLabel(
                        projectId,
                        labelId));
    }
}