package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PrestazioneDirettaAdminResponse {
    private Integer id;
    private String nome;
    private boolean attivo;
    private Integer idCategoria;
    private String nomeCategoria;
    private Integer idSpecializzazioneMedico;
    private String nomeSpecializzazioneMedico;
    private Integer idSpecializzazioneTecnico;
    private String nomeSpecializzazioneTecnico;
}