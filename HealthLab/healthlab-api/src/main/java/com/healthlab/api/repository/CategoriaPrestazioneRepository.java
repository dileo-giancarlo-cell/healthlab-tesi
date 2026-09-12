package com.healthlab.api.repository;

import com.healthlab.api.entity.CategoriaPrestazione;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;


public interface CategoriaPrestazioneRepository extends JpaRepository<CategoriaPrestazione, Integer> {
    List<CategoriaPrestazione> findAll();
    Optional<CategoriaPrestazione> findBySlug(String slug);
    List<CategoriaPrestazione> findByAttivoTrue();
}