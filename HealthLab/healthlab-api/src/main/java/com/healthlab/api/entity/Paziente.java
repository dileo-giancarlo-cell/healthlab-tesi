package com.healthlab.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "paziente")
@Getter
@Setter
@NoArgsConstructor
public class Paziente {

    @Id
    private Integer id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_utente")
    private Utente utente;

    @Enumerated(EnumType.STRING)
    @Column(length = 1)
    private Sesso sesso;

    @Column(name = "consenso_trattamento_dati", nullable = false)
    private boolean consensoTrattamentoDati = false;

    @Column(name = "data_consenso")
    private LocalDateTime dataConsenso;

    public enum Sesso {
        M, F
    }
}