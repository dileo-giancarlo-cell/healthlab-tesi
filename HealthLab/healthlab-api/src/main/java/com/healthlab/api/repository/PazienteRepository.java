package com.healthlab.api.repository;

import com.healthlab.api.entity.Paziente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PazienteRepository extends JpaRepository<Paziente, Integer> {

    // RF-09: ricerca libera per nome, cognome o codice fiscale (solo dati
    // anagrafici). Query vuota = tutti i pazienti.
    @Query("""
        SELECT p FROM Paziente p
        WHERE LOWER(p.utente.nome) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(p.utente.cognome) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(p.utente.codiceFiscale) LIKE LOWER(CONCAT('%', :query, '%'))
        ORDER BY p.utente.cognome ASC, p.utente.nome ASC
        """)
    List<Paziente> ricercaPerNomeCognomeCf(@Param("query") String query);

    // RF-19: distribuzione demografica aggregata (tira fuori un conteggio distinto per sesso)
    @Query("SELECT p.sesso, COUNT(p) FROM Paziente p GROUP BY p.sesso")
    List<Object[]> countPerSesso();
}