package com.healthlab.api.controller;

import com.healthlab.api.dto.response.DashboardPazienteResponse;
import com.healthlab.api.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/paziente")
    public DashboardPazienteResponse getPaziente() {
        return dashboardService.getDashboardPaziente();
    }
}