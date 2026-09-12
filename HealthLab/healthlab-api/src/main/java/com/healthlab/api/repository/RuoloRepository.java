package com.healthlab.api.repository;

import com.healthlab.api.entity.Ruolo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RuoloRepository extends JpaRepository<Ruolo, Integer> {
    java.util.Optional<Ruolo> findByNome(String nome);
}