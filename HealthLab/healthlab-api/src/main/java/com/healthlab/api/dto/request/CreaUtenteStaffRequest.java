package com.healthlab.api.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreaUtenteStaffRequest {
    private String nome;
    private String cognome;
    private String username;
    private String email;
    private String telefono;
    private String password;
    // "segretario" | "tecnico" | "medico"
    private String ruolo;
    private Integer idSede;
    // Non pertinente per il Segretario
    private Integer idSpecializzazione;
    // Solo Medico, entrambi opzionali
    private String numeroAlbo;
    private String biografia;
}