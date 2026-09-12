package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DaRefertareItemResponse {
    private Integer idPrenotazione;
    private LocalDateTime dataOra;
    private String codiceFiscalePaziente;
    private String nomeCognomePaziente;
    private String nomeServizio;
}