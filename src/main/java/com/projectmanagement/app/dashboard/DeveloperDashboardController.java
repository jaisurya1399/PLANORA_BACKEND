package com.projectmanagement.app.dashboard;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DeveloperDashboardController {

    private final DeveloperDashboardService developerDashboardService;

    @GetMapping("/developer")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DeveloperDashboardResponse> getDeveloperDashboard() {

        DeveloperDashboardResponse response = developerDashboardService.getDashboard();

        return ResponseEntity.ok(response);
    }
}