package com.healthlab.api.repository;

import com.healthlab.api.entity.Referto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RefertoRepository extends JpaRepository<Referto, Integer> {
    List<Referto> findByPrenotazione_Paziente_Id(Integer idPaziente);
    List<Referto> findByPrenotazione_Paziente_IdOrderByDataCaricamentoDesc(Integer idPaziente);
    long countByPrenotazione_Paziente_IdAndDataCaricamentoAfter(Integer idPaziente, LocalDateTime dopo);
    boolean existsByPrenotazione_Id(Integer idPrenotazione);
}