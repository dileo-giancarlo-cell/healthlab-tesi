package com.healthlab.api.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EsitoVisitaRequest {
    private String note;
    private boolean conclusa;
    // Valorizzato solo se il medico decide di autorizzare una prestazione vincolata
    private Integer idPrestazionePreviaVisitaAutorizzata;
}