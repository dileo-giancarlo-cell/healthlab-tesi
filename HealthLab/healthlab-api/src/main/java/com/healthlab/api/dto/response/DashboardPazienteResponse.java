package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardPazienteResponse {
    private String nome;
    private PrenotazioneResponse prossimaPrenotazione;   // null se non ce n'è nessuna
    private int refertiRecenti;                           // referti caricati negli ultimi 7 giorni
}