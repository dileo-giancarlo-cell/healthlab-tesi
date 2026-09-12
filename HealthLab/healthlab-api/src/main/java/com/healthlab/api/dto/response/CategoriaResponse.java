package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoriaResponse {
    private Integer id;
    private String nome;
    private String slug;
    private String urlImmagine;
}