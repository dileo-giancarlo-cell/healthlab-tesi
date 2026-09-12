package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MeseConteggioResponse {
    // Formato "YYYY-MM", es. "2026-03" — ordinabile alfabeticamente senza
    // bisogno di riparsare le date lato frontend.
    private String meseAnno;
    private long conteggio;
}