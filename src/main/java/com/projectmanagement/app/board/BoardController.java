package com.projectmanagement.app.board;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/boards")
public class BoardController {
    private final BoardService service;

    public BoardController(BoardService service) {
        this.service = service;
    }

    @GetMapping("/project/{projectId}/config")
    @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
    public ResponseEntity<BoardConfigResponse> config(@PathVariable Long projectId) {
        return ResponseEntity.ok(service.getConfig(projectId));
    }

    @PutMapping("/project/{projectId}/config")
    @PreAuthorize("hasAuthority('ticket.update') or hasRole('ADMIN')")
    public ResponseEntity<BoardConfigResponse> saveConfig(@PathVariable Long projectId,
            @Valid @RequestBody BoardConfigRequest request) {
        return ResponseEntity.ok(service.saveConfig(projectId, request));
    }

    @GetMapping("/project/{projectId}/columns")
    @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
    public ResponseEntity<List<BoardColumnConfigResponse>> columns(@PathVariable Long projectId) {
        return ResponseEntity.ok(service.getColumns(projectId));
    }

    @PutMapping("/project/{projectId}/columns")
    @PreAuthorize("hasAuthority('ticket.update') or hasRole('ADMIN')")
    public ResponseEntity<List<BoardColumnConfigResponse>> saveColumns(@PathVariable Long projectId,
            @Valid @RequestBody List<BoardColumnRequest> request) {
        return ResponseEntity.ok(service.saveColumns(projectId, request));
    }

    @GetMapping("/project/{projectId}/history")
    @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
    public ResponseEntity<List<BoardStatusHistory>> history(@PathVariable Long projectId,
            @RequestParam(defaultValue = "30") Integer days) {
        return ResponseEntity.ok(service.history(projectId, days));
    }

    @GetMapping("/project/{projectId}/cumulative-flow")
    @PreAuthorize("hasAuthority('ticket.view') or hasRole('ADMIN')")
    public ResponseEntity<List<CumulativeFlowPointResponse>> cumulativeFlow(@PathVariable Long projectId,
            @RequestParam(defaultValue = "30") Integer days) {
        return ResponseEntity.ok(service.cumulativeFlow(projectId, days));
    }
}
