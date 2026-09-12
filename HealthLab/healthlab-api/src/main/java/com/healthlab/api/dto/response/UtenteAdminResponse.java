package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UtenteAdminResponse {
    private Integer id;
    private String nome;
    private String cognome;
    private String username;
    private String email;
    private String telefono;
    private String ruolo;
    private boolean disabilitato;
    private boolean emailVerificata;
    // I tre campi sotto sono null quando non pertinenti al ruolo
    // (es. nomeSpecializzazione è sempre null per il Segretario)
    private String nomeSede;
    private String nomeSpecializzazione;
    private String numeroAlbo;
}