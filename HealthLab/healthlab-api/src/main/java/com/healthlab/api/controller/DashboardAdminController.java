package com.healthlab.api.controller;

import com.healthlab.api.dto.response.DashboardAdminResponse;
import com.healthlab.api.service.DashboardAdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardAdminController {

    private final DashboardAdminService dashboardAdminService;

    public DashboardAdminController(DashboardAdminService dashboardAdminService) {
        this.dashboardAdminService = dashboardAdminService;
    }

    @GetMapping
    public DashboardAdminResponse getDashboard(
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer anno) {
        int annoEffettivo = anno != null ? anno : java.time.LocalDate.now().getYear();
        return dashboardAdminService.getDashboard(annoEffettivo);
    }
}