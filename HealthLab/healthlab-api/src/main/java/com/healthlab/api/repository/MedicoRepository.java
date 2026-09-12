package com.healthlab.api.repository;

import com.healthlab.api.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface MedicoRepository extends JpaRepository<Medico, Integer> {

    long countBySpecializzazione_Id(Integer idSpecializzazione);

    @Query("""
        SELECT COUNT(m) FROM Medico m
        WHERE m.specializzazione.id = :idSpecializzazione
        AND m NOT IN (
            SELECT f.medico FROM FerieMedico f
            WHERE :data BETWEEN f.dataInizio AND f.dataFine
        )
        """)
    long countDisponibiliInData(
            @Param("idSpecializzazione") Integer idSpecializzazione,
            @Param("data") LocalDate data);

    // Query che si occupa del calcolo della disponibilità di un meidco per uno slot, 
    // tenendo conto della sede di appartenenza, della data e di eventuali ferie.
    @Query("""
        SELECT COUNT(m) FROM Medico m
        WHERE m.specializzazione.id = :idSpecializzazione
        AND m.sede.id = :idSede
        AND m NOT IN (
            SELECT f.medico FROM FerieMedico f
            WHERE :data BETWEEN f.dataInizio AND f.dataFine
        )
        """)
    long countDisponibiliInDataESede(
            @Param("idSpecializzazione") Integer idSpecializzazione,
            @Param("idSede") Integer idSede,
            @Param("data") LocalDate data);

    // Medici con la specializzazione richiesta che NON hanno già una
    // prenotazione attiva in quello slot e non sono in ferie quel
    @Query("""
        SELECT m FROM Medico m
        WHERE m.specializzazione.id = :idSpecializzazione
        AND m NOT IN (
            SELECT p.medico FROM Prenotazione p
            WHERE p.medico IS NOT NULL
            AND p.dataOra = :dataOra
            AND p.annullata = false
        )
        AND m NOT IN (
            SELECT f.medico FROM FerieMedico f
            WHERE :data BETWEEN f.dataInizio AND f.dataFine
        )
        ORDER BY m.id ASC
        """)
    List<Medico> findMediciLiberiInSlot(
            @Param("idSpecializzazione") Integer idSpecializzazione,
            @Param("dataOra") LocalDateTime dataOra,
            @Param("data") LocalDate data);

    // Come la precedente query, ma filtrata anche per sede
    // usata per l'auto-assegnazione "primo libero" del paziente/segreteria/medico.
    @Query("""
        SELECT m FROM Medico m
        WHERE m.specializzazione.id = :idSpecializzazione
        AND m.sede.id = :idSede
        AND m NOT IN (
            SELECT p.medico FROM Prenotazione p
            WHERE p.medico IS NOT NULL
            AND p.dataOra = :dataOra
            AND p.annullata = false
        )
        AND m NOT IN (
            SELECT f.medico FROM FerieMedico f
            WHERE :data BETWEEN f.dataInizio AND f.dataFine
        )
        ORDER BY m.id ASC
        """)
    List<Medico> findMediciLiberiInSlotESede(
            @Param("idSpecializzazione") Integer idSpecializzazione,
            @Param("idSede") Integer idSede,
            @Param("dataOra") LocalDateTime dataOra,
            @Param("data") LocalDate data);
}