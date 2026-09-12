package com.healthlab.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tecnico")
@Getter
@Setter
@NoArgsConstructor
public class Tecnico {

    @Id
    private Integer id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_utente")
    private Utente utente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_specializzazione", nullable = false)
    private SpecializzazioneTecnico specializzazione;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_sede", nullable = false)
    private Sede sede;
}