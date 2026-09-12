package com.healthlab.api.controller;

import com.healthlab.api.dto.response.CategoriaResponse;
import com.healthlab.api.service.CategoriaService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/categorie")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public List<CategoriaResponse> getTutte() {
        return categoriaService.getTutte();
    }

    @GetMapping("/immagine/{nomeFile}")
    public ResponseEntity<Resource> getImmagine(@PathVariable String nomeFile) throws IOException {
        return categoriaService.getImmagine(nomeFile);
    }

    @GetMapping("/{slug}")
    public CategoriaResponse getBySlug(@PathVariable String slug) {
        return categoriaService.getBySlug(slug);
    }
}