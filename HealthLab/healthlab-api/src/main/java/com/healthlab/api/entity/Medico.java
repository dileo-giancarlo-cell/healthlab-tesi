package com.healthlab.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "medico")
@Getter
@Setter
@NoArgsConstructor
public class Medico {

    @Id
    private Integer id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_utente")
    private Utente utente;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_specializzazione", nullable = false)
    private SpecializzazioneMedico specializzazione;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_sede", nullable = false)
    private Sede sede;

    @Column(name = "numero_albo", length = 25)
    private String numeroAlbo;

    @Column(columnDefinition = "TEXT")
    private String biografia;
}