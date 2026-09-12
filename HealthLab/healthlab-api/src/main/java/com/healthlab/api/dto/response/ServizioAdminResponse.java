package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ServizioAdminResponse {
    private Integer id;
    private String nome;
    private boolean attivo;
}