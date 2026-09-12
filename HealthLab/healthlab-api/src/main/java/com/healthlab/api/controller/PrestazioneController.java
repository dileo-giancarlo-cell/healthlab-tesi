package com.healthlab.api.controller;

import com.healthlab.api.dto.response.PrestazioneResponse;
import com.healthlab.api.dto.response.ServizioResponse;
import com.healthlab.api.service.PrestazioneService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prestazioni")
public class PrestazioneController {

    private final PrestazioneService prestazioneService;

    public PrestazioneController(PrestazioneService prestazioneService) {
        this.prestazioneService = prestazioneService;
    }

    @GetMapping("/categoria/{idCategoria}")
    public List<PrestazioneResponse> getByCategoria(
            @PathVariable Integer idCategoria,
            @RequestParam(required = false) Integer idSede) {
        return prestazioneService.getByCategoria(idCategoria, idSede);
    }

    @GetMapping("/{id}/servizi")
    public List<ServizioResponse> getServizi(@PathVariable Integer id) {
        return prestazioneService.getServiziByPrestazione(id);
    }
}