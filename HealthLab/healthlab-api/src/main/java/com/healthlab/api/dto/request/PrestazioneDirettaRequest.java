package com.healthlab.api.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrestazioneDirettaRequest {
    private String nome;
    private Integer idCategoria;
    // Esattamente uno dei due va valorizzato — validato nel service
    private Integer idSpecializzazioneMedico;
    private Integer idSpecializzazioneTecnico;
}