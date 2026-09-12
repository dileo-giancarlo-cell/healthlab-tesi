package com.healthlab.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "prenotazione")
@Getter
@Setter
@NoArgsConstructor
public class Prenotazione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Nullable: valorizzato solo se la prenotazione è una visita medica
    @ManyToOne
    @JoinColumn(name = "id_medico")
    private Medico medico;

    // Nullable: valorizzato solo se la prenotazione è eseguita da un tecnico
    @ManyToOne
    @JoinColumn(name = "id_tecnico")
    private Tecnico tecnico;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_paziente", nullable = false)
    private Paziente paziente;

    // uno tra questi due deve essere valorizzato
    // (vincolo CHECK già applicato a livello di database in schema.sql)
    @ManyToOne
    @JoinColumn(name = "id_prestazione_diretta")
    private PrestazioneDiretta prestazioneDiretta;

    @ManyToOne
    @JoinColumn(name = "id_prestazione_previa_visita")
    private PrestazionePreviaVisita prestazionePreviaVisita;

    @Column(name = "data_ora", nullable = false)
    private LocalDateTime dataOra;

    @Column(nullable = false)
    private boolean annullata = false;

    // Se valorizzato--> il tecnico ha chiuso la prenotazione senza caricare un
    // referto (es. paziente non presentato), con la motivazione qui dentro.
    // NULL = comportamento normale, nessuna chiusura anomala.
    @Column(name = "motivo_mancato_referto", columnDefinition = "TEXT")
    private String motivoMancatoReferto;
}