package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ServizioSedeResponse {
    private Integer id;
    private String nome;
    // "diretta" | "previa_visita"
    private String tipo;
    private String nomeCategoria;
    private boolean disponibileInSede;
}