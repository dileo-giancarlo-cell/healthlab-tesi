package com.healthlab.api.repository;

import com.healthlab.api.entity.SedePrestazionePreviaVisita;
import com.healthlab.api.entity.SedePrestazionePreviaVisitaId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SedePrestazionePreviaVisitaRepository extends JpaRepository<SedePrestazionePreviaVisita, SedePrestazionePreviaVisitaId> {
    List<SedePrestazionePreviaVisita> findBySede_Id(Integer idSede);
    boolean existsBySede_IdAndPrestazionePreviaVisita_Id(Integer idSede, Integer idPrestazionePreviaVisita);
    void deleteBySede_IdAndPrestazionePreviaVisita_Id(Integer idSede, Integer idPrestazionePreviaVisita);
}