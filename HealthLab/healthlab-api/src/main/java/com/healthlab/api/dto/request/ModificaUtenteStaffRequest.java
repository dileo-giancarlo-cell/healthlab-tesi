package com.healthlab.api.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModificaUtenteStaffRequest {
    private String nome;
    private String cognome;
    private String email;
    private String telefono;
    // Opzionale(valorizzata solo se l'Admin vuole impostare una nuova password)
    private String nuovaPassword;
    private Integer idSede;
    private Integer idSpecializzazione;
    private String numeroAlbo;
    private String biografia;
}