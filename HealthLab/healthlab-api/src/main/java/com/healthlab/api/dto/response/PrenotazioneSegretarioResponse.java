package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PrenotazioneSegretarioResponse {
    private Integer idPrenotazione;
    private LocalDateTime dataOra;
    private String nomePaziente;
    private String codiceFiscalePaziente;
    private String nomeServizio;
    // Nome dell'esecutore assegnato (medico o tecnico)
    private String nomeEsecutore;
    // "MEDICO" | "TECNICO" — dice al frontend quale  risorse mostrare
    // in fase di riassegnazione.
    private String tipoEsecutore;
}