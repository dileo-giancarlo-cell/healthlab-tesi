package com.healthlab.api.repository;

import com.healthlab.api.entity.PrestazionePreviaVisita;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrestazionePreviaVisitaRepository extends JpaRepository<PrestazionePreviaVisita, Integer> {
    // Admin: vede tutto, comprese le sospese
    List<PrestazionePreviaVisita> findByCategoriaPrestazione_Id(Integer idCategoria);

    // Lato pubblico: solo le attive
    List<PrestazionePreviaVisita> findByCategoriaPrestazione_IdAndAttivoTrue(Integer idCategoria);

    // Prestazioni sbloccate da una specifica visita propedeutica
    // (RF-14, il Medico che autorizza).
    List<PrestazionePreviaVisita> findByVisitaPropedeutica_Id(Integer idVisitaPropedeutica);
    List<PrestazionePreviaVisita> findByVisitaPropedeutica_IdAndAttivoTrue(Integer idVisitaPropedeutica);
}