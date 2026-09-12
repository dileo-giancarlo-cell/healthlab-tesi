package com.healthlab.api.repository;

import com.healthlab.api.entity.Tecnico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface TecnicoRepository extends JpaRepository<Tecnico, Integer> {

    long countBySpecializzazione_Id(Integer idSpecializzazione);

    @Query("""
        SELECT COUNT(t) FROM Tecnico t
        WHERE t.specializzazione.id = :idSpecializzazione
        AND t NOT IN (
            SELECT f.tecnico FROM FerieTecnico f
            WHERE :data BETWEEN f.dataInizio AND f.dataFine
        )
        """)
    long countDisponibiliInData(
            @Param("idSpecializzazione") Integer idSpecializzazione,
            @Param("data") LocalDate data);

    // Capacita reale per data e sede: tecnici con la specializzazione
    // richiesta, in una data sede e non in ferie in quel dato giorno.
    @Query("""
        SELECT COUNT(t) FROM Tecnico t
        WHERE t.specializzazione.id = :idSpecializzazione
        AND t.sede.id = :idSede
        AND t NOT IN (
            SELECT f.tecnico FROM FerieTecnico f
            WHERE :data BETWEEN f.dataInizio AND f.dataFine
        )
        """)
    long countDisponibiliInDataESede(
            @Param("idSpecializzazione") Integer idSpecializzazione,
            @Param("idSede") Integer idSede,
            @Param("data") LocalDate data);

    @Query("""
        SELECT t FROM Tecnico t
        WHERE t.specializzazione.id = :idSpecializzazione
        AND t NOT IN (
            SELECT p.tecnico FROM Prenotazione p
            WHERE p.tecnico IS NOT NULL
            AND p.dataOra = :dataOra
            AND p.annullata = false
        )
        AND t NOT IN (
            SELECT f.tecnico FROM FerieTecnico f
            WHERE :data BETWEEN f.dataInizio AND f.dataFine
        )
        ORDER BY t.id ASC
        """)
    List<Tecnico> findTecniciLiberiInSlot(
            @Param("idSpecializzazione") Integer idSpecializzazione,
            @Param("dataOra") LocalDateTime dataOra,
            @Param("data") LocalDate data);

    // query simile, filtrata anche per sede — 
    // usata per l'auto-assegnazione "primo libero" (RF-13/14).
    @Query("""
        SELECT t FROM Tecnico t
        WHERE t.specializzazione.id = :idSpecializzazione
        AND t.sede.id = :idSede
        AND t NOT IN (
            SELECT p.tecnico FROM Prenotazione p
            WHERE p.tecnico IS NOT NULL
            AND p.dataOra = :dataOra
            AND p.annullata = false
        )
        AND t NOT IN (
            SELECT f.tecnico FROM FerieTecnico f
            WHERE :data BETWEEN f.dataInizio AND f.dataFine
        )
        ORDER BY t.id ASC
        """)
    List<Tecnico> findTecniciLiberiInSlotESede(
            @Param("idSpecializzazione") Integer idSpecializzazione,
            @Param("idSede") Integer idSede,
            @Param("dataOra") LocalDateTime dataOra,
            @Param("data") LocalDate data);
}