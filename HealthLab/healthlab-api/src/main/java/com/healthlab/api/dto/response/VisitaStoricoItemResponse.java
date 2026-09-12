package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class VisitaStoricoItemResponse {
    private Integer idPrenotazione;
    private LocalDateTime dataOra;
    private String nomeServizio;
    private String note;
    private boolean conclusa;
    private boolean autorizzatoStepSuccessivo;
    // Nome della prestazione autorizzata, null se non è stata concessa nessuna autorizzazione
    private String nomePrestazioneAutorizzata;
}