package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ProfiloResponse {
    private String nome;
    private String cognome;
    private String email;
    private String codiceFiscale;
    private LocalDate dataNascita;
    private String sesso;
    private int totalePrenotazioni;
    private int totaleReferti;
}