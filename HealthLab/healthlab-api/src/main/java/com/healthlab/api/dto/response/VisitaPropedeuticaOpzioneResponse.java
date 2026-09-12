package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VisitaPropedeuticaOpzioneResponse {
    private Integer id;
    private String nome;
    private String nomeCategoria;
}