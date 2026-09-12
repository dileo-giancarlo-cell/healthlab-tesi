package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class GiorniConPrenotazioniResponse {
    // Giorni del mese (1-31) in cui il tecnico ha almeno una prenotazione attiva
    private Map<Integer, Long> conteggioPerGiorno;
}