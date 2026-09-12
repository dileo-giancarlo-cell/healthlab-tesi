package com.healthlab.api.service;

import com.healthlab.api.dto.response.CategoriaResponse;
import com.healthlab.api.entity.CategoriaPrestazione;
import com.healthlab.api.repository.CategoriaPrestazioneRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaPrestazioneRepository categoriaRepository;
    private final FileStorageService fileStorageService;

    public CategoriaService(CategoriaPrestazioneRepository categoriaRepository,
                             FileStorageService fileStorageService) {
        this.categoriaRepository = categoriaRepository;
        this.fileStorageService = fileStorageService;
    }

    // RF-17: lato pubblico, solo le categorie attive - una sospesa non deve
    //  comparire nell'elenco da cui il paziente sceglie.
    public List<CategoriaResponse> getTutte() {
        return categoriaRepository.findByAttivoTrue().stream()
                .map(c -> new CategoriaResponse(c.getId(), c.getNome(), c.getSlug(), c.getUrlImmagine()))
                .toList();
    }

    // RF-17: se qualcuno arriva qui con lo slug di una categoria sospesa
    // (es. link salvato, o chiamata diretta all'API), blocchiamo con un
    // errore parlante.
    public CategoriaResponse getBySlug(String slug) {
        CategoriaPrestazione categoria = categoriaRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Categoria non trovata: " + slug));

        if (!categoria.isAttivo()) {
            throw new IllegalArgumentException("Questa categoria non è al momento disponibile.");
        }

        return new CategoriaResponse(categoria.getId(), categoria.getNome(), categoria.getSlug(), categoria.getUrlImmagine());
    }


    public ResponseEntity<Resource> getImmagine(String nomeFile) throws IOException {
        Path percorso = fileStorageService.getPercorsoCompletoImmagineCategoria(nomeFile);
        Resource risorsa = new UrlResource(percorso.toUri());

        if (!risorsa.exists()) {
            throw new IllegalArgumentException("Immagine non trovata");
        }

        String contentType = Files.probeContentType(percorso);
        MediaType mediaType = (contentType != null)
                ? MediaType.parseMediaType(contentType)
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(risorsa);
    }
}