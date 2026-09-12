package com.healthlab.api.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrestazionePreviaVisitaRequest {
    private String nome;
    private Integer idCategoria;
    private Integer idVisitaPropedeutica;
    //  uno dei due va valorizzato — validato nel service
    private Integer idSpecializzazioneMedico;
    private Integer idSpecializzazioneTecnico;
}