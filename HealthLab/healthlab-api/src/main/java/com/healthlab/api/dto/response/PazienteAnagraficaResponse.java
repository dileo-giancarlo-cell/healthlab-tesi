package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PazienteAnagraficaResponse {
    private Integer idPaziente;
    private String nome;
    private String cognome;
    private String codiceFiscale;
    private String telefono;
    private String email;
    // false = registrazione da completare (account creato dalla Segreteria e il
    // paziente non ha ancora impostato una password propria)
    private boolean emailVerificata;
}