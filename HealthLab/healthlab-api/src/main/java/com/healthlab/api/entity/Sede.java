package com.healthlab.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sede")
@Getter
@Setter
@NoArgsConstructor
public class Sede {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 255)
    private String indirizzo;

   
    @Column(length = 255)
    private String orario;

    @Column(length = 30)
    private String telefono;
}