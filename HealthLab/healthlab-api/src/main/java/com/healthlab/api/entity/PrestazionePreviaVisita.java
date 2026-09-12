package com.healthlab.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "prestazione_previa_visita")
@Getter
@Setter
@NoArgsConstructor
public class PrestazionePreviaVisita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_categoria_prestazione", nullable = false)
    private CategoriaPrestazione categoriaPrestazione;

    // Punta alla visita specialistica (in Prestazione_Diretta) che sblocca
    // la prestazione vincolata - es. "Rimozione lipoma" -> "Visita dermatologica".
    // Usata solo per il controllo di autorizzazione (RF-04)
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_visita_propedeutica", nullable = false)
    private PrestazioneDiretta visitaPropedeutica;

    // Solo uno dei due ID deve essere valorizzato (CHECK a livello di database):
    // dice chi esegue materialmente la prestazione vincolata.
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

    @Transient
    public boolean isEseguitaDaTecnico() {
        return specializzazioneTecnico != null;
    }
}