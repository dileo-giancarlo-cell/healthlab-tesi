package com.healthlab.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "prestazione_diretta_servizi")
@Getter
@Setter
@NoArgsConstructor
public class PrestazioneDirettaServizi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_prestazione_diretta", nullable = false)
    private PrestazioneDiretta prestazioneDiretta;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false)
    private boolean attivo = true;
}