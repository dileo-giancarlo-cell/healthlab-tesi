package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoriaAdminResponse {
    private Integer id;
    private String nome;
    private String slug;
    private boolean attivo;
    private String urlImmagine;
}