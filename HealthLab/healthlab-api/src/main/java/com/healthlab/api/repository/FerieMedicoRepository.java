package com.healthlab.api.repository;

import com.healthlab.api.entity.FerieMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface FerieMedicoRepository extends JpaRepository<FerieMedico, Integer> {

    List<FerieMedico> findByMedico_IdOrderByDataInizioDesc(Integer idMedico);

    // Prenotazioni attive assegnate al medico nel periodo selezionato
    // Usato per bloccare l'inserimento di ferie se vi sono prenotazioni attive (RF-10, Opzione A).
    @Query("""
        SELECT COUNT(p) FROM Prenotazione p
        WHERE p.medico.id = :idMedico
        AND p.annullata = false
        AND p.dataOra >= :dataInizio
        AND p.dataOra < :dataFineEsclusiva
        """)
    long contaPrenotazioniAttiveNelPeriodo(
            @Param("idMedico") Integer idMedico,
            @Param("dataInizio") LocalDateTime dataInizio,
            @Param("dataFineEsclusiva") LocalDateTime dataFineEsclusiva);
}