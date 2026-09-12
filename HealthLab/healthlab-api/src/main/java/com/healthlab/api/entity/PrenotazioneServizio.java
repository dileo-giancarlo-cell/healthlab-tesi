package com.healthlab.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "prenotazione_servizi")
@Getter
@Setter
@NoArgsConstructor
@IdClass(PrenotazioneServizioId.class)
public class PrenotazioneServizio {

    @Id
    @ManyToOne
    @JoinColumn(name = "id_prenotazione")
    private Prenotazione prenotazione;

    @Id
    @ManyToOne
    @JoinColumn(name = "id_servizio")
    private PrestazioneDirettaServizi servizio;
}