package com.healthlab.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "referto")
@Getter
@Setter
@NoArgsConstructor
public class Referto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_prenotazione", nullable = false)
    private Prenotazione prenotazione;

    @Column(name = "file_o_valore", nullable = false, length = 255)
    private String fileOValore;

    @Column(name = "data_caricamento", nullable = false)
    private LocalDateTime dataCaricamento = LocalDateTime.now();
}
