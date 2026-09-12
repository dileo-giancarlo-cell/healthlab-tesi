package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RisorsaConFerieResponse {
    private Integer id;
    private String nomeCompleto;
    private String tipo; // "MEDICO" | "TECNICO"
    private List<FerieResponse> ferie;
}