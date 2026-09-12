package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PazienteInCaricoResponse {
    private Integer idPaziente;
    private String nome;
    private String cognome;
    private String codiceFiscale;
    private LocalDateTime ultimaVisita;
}