package com.healthlab.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "utente")
@Getter
@Setter
@NoArgsConstructor
public class Utente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_ruolo", nullable = false)
    private Ruolo ruolo;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String nome;

    @Column(nullable = false, length = 50)
    private String cognome;

    @Column(length = 20)
    private String telefono;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "codice_fiscale", unique = true, length = 25)
    private String codiceFiscale;

    @Column(name = "data_nascita")
    private LocalDate dataNascita;

    @Column(name = "luogo_nascita", length = 50)
    private String luogoNascita;

    @Column(name = "data_creazione", nullable = false, updatable = false)
    private LocalDateTime dataCreazione = LocalDateTime.now();

    @Column(nullable = false)
    private boolean disabilitato = false;

    @Column(name = "email_verificata", nullable = false)
    private boolean emailVerificata = false;

    @Column(name = "token_verifica")
    private String tokenVerifica;

    @Column(name = "token_scadenza")
    private LocalDateTime tokenScadenza;

    @Column(name = "token_reset")
    private String tokenReset;

    @Column(name = "token_reset_scadenza")
    private LocalDateTime tokenResetScadenza;
}