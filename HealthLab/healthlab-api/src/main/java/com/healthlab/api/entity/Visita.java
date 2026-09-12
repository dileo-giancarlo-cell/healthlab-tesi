package com.healthlab.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "visita")
@Getter
@Setter
@NoArgsConstructor
public class Visita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_prenotazione", nullable = false, unique = true)
    private Prenotazione prenotazione;

    @Column(name = "autorizzato_step_successivo", nullable = false)
    private boolean autorizzatoStepSuccessivo = false;

    @Column(nullable = false)
    private boolean conclusa = false;

    @Column(name = "data_ora", nullable = false)
    private java.time.LocalDateTime dataOra;

    @Column(columnDefinition = "TEXT")
    private String note;

    @ManyToOne
    @JoinColumn(name = "id_prestazione_previa_visita_autorizzata")
    private PrestazionePreviaVisita prestazionePreviaVisitaAutorizzata;

    // l'autorizzazione vale per un solo uso. Diventa true nel
    // momento in cui viene effettivamente usata per prenotare con successo
    // la prestazione vincolata — da quel momento non conta più come "attiva"
    // E' inoltre la prova storica che l'autorizzazione fu concessa.
    @Column(name = "autorizzazione_consumata", nullable = false)
    private boolean autorizzazioneConsumata = false;
}