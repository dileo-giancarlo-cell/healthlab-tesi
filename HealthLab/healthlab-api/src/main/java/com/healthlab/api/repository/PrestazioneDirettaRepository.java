package com.healthlab.api.repository;

import com.healthlab.api.entity.PrestazioneDiretta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrestazioneDirettaRepository extends JpaRepository<PrestazioneDiretta, Integer> {
    // Admin: vede tutto, comprese le sospese (per poterle riattivare)
    List<PrestazioneDiretta> findByCategoriaPrestazione_Id(Integer idCategoria);

    // Lato pubblico: solo le attive
    List<PrestazioneDiretta> findByCategoriaPrestazione_IdAndAttivoTrue(Integer idCategoria);

    // Per il menu "visita propedeutica" quando l'Admin crea/modifica una
    // prestazione vincolata: solo le prestazioni dirette ESEGUITE DA UN MEDICO
    // (una visita propedeutica é per definizione una valutazione medica) e attive.
    List<PrestazioneDiretta> findBySpecializzazioneMedicoIsNotNullAndAttivoTrue();
}