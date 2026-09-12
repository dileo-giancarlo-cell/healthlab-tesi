package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class StoricoTecnicoItemResponse {
    private Integer idPrenotazione;
    private String codiceFiscalePaziente;
    private String nomePaziente;
    private String nomeServizio;
    private LocalDateTime dataOra;
    // REFERTATA | NON_COMPLETATA | ANNULLATA | DA_GESTIRE
    private String stato;
    // Valorizzato solo se stato = NON_COMPLETATA
    private String motivoMancatoReferto;
}