package com.healthlab.api.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoriaRequest {
    private String nome;
    private String slug;
    // Opzionale: null/vuoto significa nessuna immagine
    private String urlImmagine;
}