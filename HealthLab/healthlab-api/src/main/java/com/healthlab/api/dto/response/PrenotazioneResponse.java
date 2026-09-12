package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PrenotazioneResponse {
    private Integer id;
    private String nomeServizio;
    private LocalDateTime dataOra;
    private boolean annullata;

    // Valorizzati solo se questa prenotazione é una visita (propedeutica o
    // di altro tipo) e il medico ne ha già registrato l'esito — null in tutti
    // gli altri casi (o prenotazione non ancora avvenuta o non è una visita).
    private Boolean visitaConclusa;

    // Nome della prestazione vincolata per cui il paziente è stato autorizzato
    // durante questa visita — null se non autorizzato, o se l'autorizzazione
    // è già stata usata (evita di mostrare come "ancora valida" qualcosa che
    // il paziente ha già prenotato e consumato).
    private String nomePrestazioneAutorizzata;

    // Le note cliniche scritte dal medico durante la visita — null se non è
    // una visita, se non ancora conclusa, o se il medico non ha scritto nulla.
    private String noteVisita;
}