package com.healthlab.api.repository;

import com.healthlab.api.entity.FerieTecnico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FerieTecnicoRepository extends JpaRepository<FerieTecnico, Integer> {

    List<FerieTecnico> findByTecnico_IdOrderByDataInizioDesc(Integer idTecnico);

    @Query("""
        SELECT COUNT(p) FROM Prenotazione p
        WHERE p.tecnico.id = :idTecnico
        AND p.annullata = false
        AND p.dataOra >= :dataInizio
        AND p.dataOra < :dataFineEsclusiva
        """)
    long contaPrenotazioniAttiveNelPeriodo(
            @Param("idTecnico") Integer idTecnico,
            @Param("dataInizio") LocalDateTime dataInizio,
            @Param("dataFineEsclusiva") LocalDateTime dataFineEsclusiva);
}