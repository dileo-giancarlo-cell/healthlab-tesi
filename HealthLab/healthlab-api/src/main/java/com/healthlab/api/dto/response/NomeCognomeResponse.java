package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NomeCognomeResponse {
    private String nome;
    private String cognome;
}