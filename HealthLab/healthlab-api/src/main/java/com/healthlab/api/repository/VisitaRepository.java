package com.healthlab.api.repository;

import com.healthlab.api.entity.Visita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VisitaRepository extends JpaRepository<Visita, Integer> {


    @Query("""
        SELECT COUNT(v) > 0 FROM Visita v
        WHERE v.prenotazione.paziente.id = :idPaziente
          AND v.prestazionePreviaVisitaAutorizzata.id = :idPrestazione
          AND v.autorizzatoStepSuccessivo = true
          AND v.autorizzazioneConsumata = false
        """)
    boolean esisteAutorizzazioneAttiva(
            @Param("idPaziente") Integer idPaziente,
            @Param("idPrestazione") Integer idPrestazione
    );

    // Restituisce una lista di autorizzazioni attive (non ancora consumate) per questo paziente e
    // questa prestazione — usato per marcarle come consumate nel momento in
    // cui vengono effettivamente usate per una prenotazione riuscita.
    @Query("""
        SELECT v FROM Visita v
        WHERE v.prenotazione.paziente.id = :idPaziente
          AND v.prestazionePreviaVisitaAutorizzata.id = :idPrestazione
          AND v.autorizzatoStepSuccessivo = true
          AND v.autorizzazioneConsumata = false
        """)
    List<Visita> findAutorizzazioniAttive(
            @Param("idPaziente") Integer idPaziente,
            @Param("idPrestazione") Integer idPrestazione
    );

    // Storico visite gestite da un medico specifico (RF-13)
    List<Visita> findByPrenotazione_Medico_Id(Integer idMedico);

    // Per evitare di registrare due volte l'esito della stessa visita
    boolean existsByPrenotazione_Id(Integer idPrenotazione);

    // La visita (se esiste) collegata a una prenotazione specifica — usata per
    // mostrare al paziente, in "Le mie prenotazioni", lo stato di conclusione
    // e un'eventuale autorizzazione concessa (RF-05 esteso).
    java.util.Optional<Visita> findByPrenotazione_Id(Integer idPrenotazione);
}