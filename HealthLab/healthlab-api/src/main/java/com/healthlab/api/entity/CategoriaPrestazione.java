package com.healthlab.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categoria_prestazione")
@Getter
@Setter
@NoArgsConstructor
public class CategoriaPrestazione {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String nome;

    @Column(nullable = false, unique = true, length = 50)
    private String slug;

    @Column(nullable = false)
    private boolean attivo = true;

    @Column(name = "url_immagine", length = 500)
    private String urlImmagine;
}