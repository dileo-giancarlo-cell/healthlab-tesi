package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AgendaTecnicoItemResponse {
    private Integer idPrenotazione;
    private String nomePaziente;
    private String nomeServizio;
    private LocalDateTime dataOra;
    private boolean refertoCaricato;
    // Non null solo se il tecnico ha chiuso la prenotazione senza referto
    // (es. paziente non presentato). Permette al frontend di distinguere
    // questo caso da "referto non ancora caricato".
    private String motivoMancatoReferto;
}