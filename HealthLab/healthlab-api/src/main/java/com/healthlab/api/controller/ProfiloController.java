package com.healthlab.api.controller;

import com.healthlab.api.dto.response.ProfiloResponse;
import com.healthlab.api.service.ProfiloService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pazienti")
public class ProfiloController {

    private final ProfiloService profiloService;

    public ProfiloController(ProfiloService profiloService) {
        this.profiloService = profiloService;
    }

    @GetMapping("/me")
    public ProfiloResponse getMio() {
        return profiloService.getProfiloMio();
    }
}