package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SedeResponse {
    private Integer id;
    private String nome;
    private String indirizzo;
    private String orario;
    private String telefono;
}