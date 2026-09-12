package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class VisitaDaGestireItemResponse {
    private Integer idPrenotazione;
    private Integer idPaziente;
    private LocalDateTime dataOra;
    private String nomePaziente;
    private String codiceFiscalePaziente;
    private String nomeServizio;
}