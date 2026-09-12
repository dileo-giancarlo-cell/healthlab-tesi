package com.healthlab.api.controller;

import com.healthlab.api.dto.response.SedeResponse;
import com.healthlab.api.service.SedeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sedi")
public class SedeController {

    private final SedeService sedeService;

    public SedeController(SedeService sedeService) {
        this.sedeService = sedeService;
    }

    @GetMapping
    public List<SedeResponse> getTutte() {
        return sedeService.getTutte();
    }
}