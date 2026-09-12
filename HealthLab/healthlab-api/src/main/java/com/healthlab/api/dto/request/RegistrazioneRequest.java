package com.healthlab.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RegistrazioneRequest {

    @NotBlank
    private String nome;

    @NotBlank
    private String cognome;

    @NotBlank
    private String username;

    @NotBlank @Email
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String telefono;

    private String codiceFiscale;

    @NotNull
    private LocalDate dataNascita;

    @NotBlank
    private String sesso;   // "M" o "F"
}