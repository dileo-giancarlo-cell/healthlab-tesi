package com.healthlab.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sede_prestazione_previa_visita")
@IdClass(SedePrestazionePreviaVisitaId.class)
@Getter
@Setter
@NoArgsConstructor
public class SedePrestazionePreviaVisita {

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_sede")
    private Sede sede;

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_prestazione_previa_visita")
    private PrestazionePreviaVisita prestazionePreviaVisita;
}