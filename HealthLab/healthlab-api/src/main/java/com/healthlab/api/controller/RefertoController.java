package com.healthlab.api.controller;

import com.healthlab.api.dto.response.RefertoResponse;
import com.healthlab.api.service.RefertoService;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/referti")
public class RefertoController {

    private final RefertoService refertoService;

    public RefertoController(RefertoService refertoService) {
        this.refertoService = refertoService;
    }

    @GetMapping("/me")
    public List<RefertoResponse> getMiei() {
        return refertoService.getRefertiMiei();
    }

    @PostMapping("/prenotazione/{idPrenotazione}")
    public RefertoResponse upload(
            @PathVariable Integer idPrenotazione,
            @RequestParam("file") MultipartFile file) {
        return refertoService.caricaReferto(idPrenotazione, file);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Integer id) throws IOException {
        return refertoService.scaricaReferto(id);
    }
}