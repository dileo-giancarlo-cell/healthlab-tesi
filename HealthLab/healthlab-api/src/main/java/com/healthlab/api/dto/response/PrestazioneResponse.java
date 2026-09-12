package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PrestazioneResponse {
    private Integer id;
    private String nome;
    private String tipo;
    private boolean prenotabileDaSolo;
}