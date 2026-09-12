package com.healthlab.api.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SedeRequest {
    private String nome;
    private String indirizzo;
    private String orario;
    private String telefono;
}