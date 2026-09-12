package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class StoricoSegretarioItemResponse {
    private Integer idPrenotazione;
    private LocalDateTime dataOra;
    private String nomePaziente;
    private String codiceFiscalePaziente;
    private String nomeServizio;
    private String nomeEsecutore;
    private String tipoEsecutore;
    private String stato;
    private String motivoMancatoReferto;
}