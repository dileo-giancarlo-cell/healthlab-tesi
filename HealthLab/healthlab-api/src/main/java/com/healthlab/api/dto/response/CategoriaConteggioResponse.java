package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoriaConteggioResponse {
    private String nomeCategoria;
    private long conteggio;
}