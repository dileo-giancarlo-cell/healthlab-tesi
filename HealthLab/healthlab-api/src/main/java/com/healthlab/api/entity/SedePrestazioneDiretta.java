package com.healthlab.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sede_prestazione_diretta")
@IdClass(SedePrestazioneDirettaId.class)
@Getter
@Setter
@NoArgsConstructor
public class SedePrestazioneDiretta {

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_sede")
    private Sede sede;

    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_prestazione_diretta")
    private PrestazioneDiretta prestazioneDiretta;
}