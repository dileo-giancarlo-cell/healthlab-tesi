package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AutorizzazioneStatusResponse {
    private boolean autorizzata;
    private Integer idVisitaPropedeutica;
    private String nomeVisitaPropedeutica;
}