package com.healthlab.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "prestazione_diretta")
@Getter
@Setter
@NoArgsConstructor
public class PrestazioneDiretta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_categoria_prestazione", nullable = false)
    private CategoriaPrestazione categoriaPrestazione;

    // Solo uno tra questi due è valorizzato (CHECK a livello di db):
    // dice chi esegue materialmente la prestazione, un Medico o un Tecnico.
    @ManyToOne
    @JoinColumn(name = "id_specializzazione_medico")
    private SpecializzazioneMedico specializzazioneMedico;

    @ManyToOne
    @JoinColumn(name = "id_specializzazione_tecnico")
    private SpecializzazioneTecnico specializzazioneTecnico;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false)
    private boolean attivo = true;

    // Non mappato: derivato da quale delle due FK è valorizzata.
    @Transient
    public boolean isEseguitaDaTecnico() {
        return specializzazioneTecnico != null;
    }
}