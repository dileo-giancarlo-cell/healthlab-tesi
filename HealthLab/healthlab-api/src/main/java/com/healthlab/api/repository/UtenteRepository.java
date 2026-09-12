package com.healthlab.api.repository;

import com.healthlab.api.entity.Utente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UtenteRepository extends JpaRepository<Utente, Integer> {
    Optional<Utente> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<Utente> findByTokenVerifica(String token);
    Optional<Utente> findByEmail(String email);
    Optional<Utente> findByTokenReset(String token);

    // RF-16: elenco dello staff ( solo segretario/tecnico/medico) per l'Admin —
    // esclude volutamente paziente/admin
    List<Utente> findByRuolo_NomeIn(List<String> nomiRuolo);
}