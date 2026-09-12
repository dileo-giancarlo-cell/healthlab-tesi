package com.healthlab.api.repository;

import com.healthlab.api.entity.Prenotazione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PrenotazioneRepository extends JpaRepository<Prenotazione, Integer> {
    List<Prenotazione> findByPaziente_Id(Integer idPaziente);
    List<Prenotazione> findByMedico_Id(Integer idMedico);

    boolean existsByPaziente_IdAndPrestazioneDiretta_IdAndDataOraBetweenAndAnnullataFalse(
        Integer idPaziente, Integer idPrestazioneDiretta, LocalDateTime inizio, LocalDateTime fine);

    boolean existsByPaziente_IdAndPrestazionePreviaVisita_IdAndDataOraBetweenAndAnnullataFalse(
            Integer idPaziente, Integer idPrestazionePreviaVisita, LocalDateTime inizio, LocalDateTime fine);

    boolean existsByPrestazioneDiretta_IdAndDataOraAndAnnullataFalse(
            Integer idPrestazioneDiretta, LocalDateTime dataOra);

    boolean existsByPrestazionePreviaVisita_IdAndDataOraAndAnnullataFalse(
            Integer idPrestazionePreviaVisita, LocalDateTime dataOra);

        @Query("""
        SELECT p.dataOra FROM Prenotazione p
        WHERE p.prestazioneDiretta.id = :idServizio
        AND p.dataOra BETWEEN :inizio AND :fine
        AND p.annullata = false
        """)
        List<LocalDateTime> findOrariOccupatiDiretta(
                @Param("idServizio") Integer idServizio,
                @Param("inizio") LocalDateTime inizio,
                @Param("fine") LocalDateTime fine);

        @Query("""
        SELECT p.dataOra FROM Prenotazione p
        WHERE p.prestazionePreviaVisita.id = :idServizio
        AND p.dataOra BETWEEN :inizio AND :fine
        AND p.annullata = false
        """)
        List<LocalDateTime> findOrariOccupatiPreviaVisita(
                @Param("idServizio") Integer idServizio,
                @Param("inizio") LocalDateTime inizio,
                @Param("fine") LocalDateTime fine);

        // Occupazione di uno slot per una specializzazione MEDICA: copre sia le
        // prestazioni dirette eseguite da un medico, sia le prestazioni vincolate
        // eseguite da un medico. LEFT JOIN espliciti: una Prenotazione ha SEMPRE
        // solo una tra prestazioneDiretta/prestazionePreviaVisita valorizzata,
        // NO INNER JOIN: eliminerebbe ogni riga
      
        @Query("""
        SELECT COUNT(p) FROM Prenotazione p
        LEFT JOIN p.prestazioneDiretta pd
        LEFT JOIN pd.specializzazioneMedico pdSpecMed
        LEFT JOIN p.prestazionePreviaVisita ppv
        LEFT JOIN ppv.specializzazioneMedico ppvSpecMed
        WHERE p.dataOra = :dataOra
        AND p.annullata = false
        AND (
                (pdSpecMed IS NOT NULL AND pdSpecMed.id = :idSpecializzazione)
                OR (ppvSpecMed IS NOT NULL AND ppvSpecMed.id = :idSpecializzazione)
        )
        """)
        long countPrenotazioniAttiveBySpecializzazioneMedicoESlot(
                @Param("idSpecializzazione") Integer idSpecializzazione,
                @Param("dataOra") LocalDateTime dataOra);

        // Analoga alla precedente, ma per la capacità dei Tecnici.
        @Query("""
        SELECT COUNT(p) FROM Prenotazione p
        LEFT JOIN p.prestazioneDiretta pd
        LEFT JOIN pd.specializzazioneTecnico pdSpecTec
        LEFT JOIN p.prestazionePreviaVisita ppv
        LEFT JOIN ppv.specializzazioneTecnico ppvSpecTec
        WHERE p.dataOra = :dataOra
        AND p.annullata = false
        AND (
                (pdSpecTec IS NOT NULL AND pdSpecTec.id = :idSpecializzazione)
                OR (ppvSpecTec IS NOT NULL AND ppvSpecTec.id = :idSpecializzazione)
        )
        """)
        long countPrenotazioniAttiveBySpecializzazioneTecnicoESlot(
                @Param("idSpecializzazione") Integer idSpecializzazione,
                @Param("dataOra") LocalDateTime dataOra);
        
        @Query("""
        SELECT p FROM Prenotazione p
        WHERE p.paziente.id = :idPaziente
        AND p.annullata = false
        AND p.dataOra >= :adesso
        ORDER BY p.dataOra ASC
        """)
        List<Prenotazione> findProssimePrenotazioni(
                @Param("idPaziente") Integer idPaziente,
                @Param("adesso") LocalDateTime adesso);

        // RF-11: agenda del tecnico per una specifica giornata (fascia [inizioGiorno, fineGiorno))
        @Query("""
        SELECT p FROM Prenotazione p
        WHERE p.tecnico.id = :idTecnico
        AND p.dataOra >= :inizioGiorno
        AND p.dataOra < :fineGiorno
        AND p.annullata = false
        ORDER BY p.dataOra ASC
        """)
        List<Prenotazione> findAgendaTecnico(
                @Param("idTecnico") Integer idTecnico,
                @Param("inizioGiorno") LocalDateTime inizioGiorno,
                @Param("fineGiorno") LocalDateTime fineGiorno);

        // Query per valorizzare il Calendario mensile del tecnico. Contiene solo le date/ora delle prenotazioni,
        // così il frontend può ricavare quali giorni evidenziare senza caricare tutti i dettagli.
        @Query("""
        SELECT p.dataOra FROM Prenotazione p
        WHERE p.tecnico.id = :idTecnico
        AND p.dataOra >= :inizioMese
        AND p.dataOra < :fineMese
        AND p.annullata = false
        """)
        List<LocalDateTime> findDataOraPrenotazioniTecnicoNelMese(
                @Param("idTecnico") Integer idTecnico,
                @Param("inizioMese") LocalDateTime inizioMese,
                @Param("fineMese") LocalDateTime fineMese);

        // Query per le prenotazioni del tecnico già passate senza referto caricato
        @Query("""
        SELECT p FROM Prenotazione p
        WHERE p.tecnico.id = :idTecnico
        AND p.annullata = false
        AND p.dataOra <= :adesso
        AND p.motivoMancatoReferto IS NULL
        AND NOT EXISTS (SELECT r FROM Referto r WHERE r.prenotazione = p)
        ORDER BY p.dataOra ASC
        """)
        List<Prenotazione> findDaRefertareByTecnico(
                @Param("idTecnico") Integer idTecnico,
                @Param("adesso") LocalDateTime adesso);

        // Query che mostra tutte le prenotazioni del tecnico, ordinate dalla più recente alla meno recente.
        @Query("""
        SELECT p FROM Prenotazione p
        WHERE p.tecnico.id = :idTecnico
        AND p.dataOra <= :adesso
        ORDER BY p.dataOra DESC
        """)
        List<Prenotazione> findStoricoByTecnico(
                @Param("idTecnico") Integer idTecnico,
                @Param("adesso") LocalDateTime adesso);

        // RF-08: prenotazioni future, non annullate, della sede del Segretario
        // (tramite medico o tecnico assegnato — SOLO LEFT JOIN, stesso motivo
        // indicato in una delle prime query in alto).
        // Ricerca libera opzionale su nome/cognome/CF del paziente.
        @Query("""
        SELECT p FROM Prenotazione p
        LEFT JOIN p.medico m
        LEFT JOIN p.tecnico t
        WHERE p.annullata = false
        AND p.dataOra >= :adesso
        AND (
                (m IS NOT NULL AND m.sede.id = :idSede)
                OR (t IS NOT NULL AND t.sede.id = :idSede)
        )
        AND (
                :query = ''
                OR LOWER(p.paziente.utente.nome) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(p.paziente.utente.cognome) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(p.paziente.utente.codiceFiscale) LIKE LOWER(CONCAT('%', :query, '%'))
        )
        ORDER BY p.dataOra ASC
        """)
        List<Prenotazione> findPerSegretario(
                @Param("idSede") Integer idSede,
                @Param("adesso") LocalDateTime adesso,
                @Param("query") String query);

        // Query che mostra tutte le prenotazioni della sede del Segretario, ordinate dalla più recente alla meno recente e anche quelle annullate
        // (quadro completo, a differenza di findPerSegretario che è
        // l'agenda operativa e mostra solo il futuro attivo). Stessi LEFT JOIN
        // espliciti per lo stesso motivo di sempre.
        @Query("""
        SELECT p FROM Prenotazione p
        LEFT JOIN p.medico m
        LEFT JOIN p.tecnico t
        WHERE p.dataOra < :adesso
        AND (
                (m IS NOT NULL AND m.sede.id = :idSede)
                OR (t IS NOT NULL AND t.sede.id = :idSede)
        )
        AND (
                :query = ''
                OR LOWER(p.paziente.utente.nome) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(p.paziente.utente.cognome) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(p.paziente.utente.codiceFiscale) LIKE LOWER(CONCAT('%', :query, '%'))
        )
        ORDER BY p.dataOra DESC
        """)
        List<Prenotazione> findStoricoPerSegretario(
                @Param("idSede") Integer idSede,
                @Param("adesso") LocalDateTime adesso,
                @Param("query") String query);

        // RF-14: prenotazioni associate ad un medico passate, non annullate, per le quali non è ancora stato
        // registrato un esito (nessuna Visita collegata).
        @Query("""
        SELECT p FROM Prenotazione p
        WHERE p.medico.id = :idMedico
        AND p.annullata = false
        AND p.dataOra <= :adesso
        AND NOT EXISTS (SELECT v FROM Visita v WHERE v.prenotazione = p)
        ORDER BY p.dataOra ASC
        """)
        List<Prenotazione> findVisiteDaGestireByMedico(
                @Param("idMedico") Integer idMedico,
                @Param("adesso") LocalDateTime adesso);

        // Prossimi appuntamenti del medico (RF-13/14).
        @Query("""
        SELECT p FROM Prenotazione p
        WHERE p.medico.id = :idMedico
        AND p.annullata = false
        AND p.dataOra > :adesso
        ORDER BY p.dataOra ASC
        """)
        List<Prenotazione> findProssimiAppuntamentiMedico(
                @Param("idMedico") Integer idMedico,
                @Param("adesso") LocalDateTime adesso);

        // ========== RF-19: dashboard analitica Admin ==========
        // Tutte le query qui sotto restituiscono SOLO conteggi aggregati — mai
        // righe con dati che permettano di indentificare anche un solo paziente (problema privacy e GDPR)

        long countByDataOraLessThanEqual(LocalDateTime adesso);
        long countByDataOraLessThanEqualAndAnnullataFalse(LocalDateTime adesso);

        // Stesse tre query di sopra, ma filtrate per anno — usate quando
        // l'Admin scorre il selettore anno: prima solo il grafico mensile
        // rifletteva l'anno scelto, ora anche i 4 valori riassuntivi in alto.
        @Query("SELECT COUNT(p) FROM Prenotazione p WHERE YEAR(p.dataOra) = :anno")
        long countByAnno(@Param("anno") int anno);

        @Query("SELECT COUNT(p) FROM Prenotazione p WHERE YEAR(p.dataOra) = :anno AND p.dataOra <= :adesso")
        long countByAnnoAndDataOraLessThanEqual(@Param("anno") int anno, @Param("adesso") LocalDateTime adesso);

        @Query("""
        SELECT COUNT(p) FROM Prenotazione p
        WHERE YEAR(p.dataOra) = :anno AND p.dataOra <= :adesso AND p.annullata = false
        """)
        long countByAnnoAndDataOraLessThanEqualAndAnnullataFalse(
                @Param("anno") int anno, @Param("adesso") LocalDateTime adesso);

        // Distribuzione per categoria: due query separate (diretta/previa
        // visita) perché sono due catene di relazione diverse — il merge dei
        // conteggi per nome categoria avviene in Java, nel service.
        @Query("""
        SELECT p.prestazioneDiretta.categoriaPrestazione.nome, COUNT(p)
        FROM Prenotazione p
        WHERE p.prestazioneDiretta IS NOT NULL AND p.annullata = false
        GROUP BY p.prestazioneDiretta.categoriaPrestazione.nome
        """)
        List<Object[]> countPerCategoriaDiretta();

        @Query("""
        SELECT p.prestazionePreviaVisita.categoriaPrestazione.nome, COUNT(p)
        FROM Prenotazione p
        WHERE p.prestazionePreviaVisita IS NOT NULL AND p.annullata = false
        GROUP BY p.prestazionePreviaVisita.categoriaPrestazione.nome
        """)
        List<Object[]> countPerCategoriaPreviaVisita();

        // Stesse due query di sopra, filtrate per anno.
        @Query("""
        SELECT p.prestazioneDiretta.categoriaPrestazione.nome, COUNT(p)
        FROM Prenotazione p
        WHERE p.prestazioneDiretta IS NOT NULL AND p.annullata = false AND YEAR(p.dataOra) = :anno
        GROUP BY p.prestazioneDiretta.categoriaPrestazione.nome
        """)
        List<Object[]> countPerCategoriaDirettaAnno(@Param("anno") int anno);

        @Query("""
        SELECT p.prestazionePreviaVisita.categoriaPrestazione.nome, COUNT(p)
        FROM Prenotazione p
        WHERE p.prestazionePreviaVisita IS NOT NULL AND p.annullata = false AND YEAR(p.dataOra) = :anno
        GROUP BY p.prestazionePreviaVisita.categoriaPrestazione.nome
        """)
        List<Object[]> countPerCategoriaPreviaVisitaAnno(@Param("anno") int anno);

        // Distribuzione demografica filtrata per anno: pazienti
        //  con almeno una prenotazione in quell'anno, per genere —
        // stessa logica "conteggio persone" della versione non filtrata
        // (PazienteRepository.countPerSesso), non "conteggio prenotazioni",
        // altrimenti un singolo paziente molto attivo distorcerebbe il grafico.
        @Query("""
        SELECT p.paziente.sesso, COUNT(DISTINCT p.paziente)
        FROM Prenotazione p
        WHERE YEAR(p.dataOra) = :anno
        GROUP BY p.paziente.sesso
        """)
        List<Object[]> countPazientiDistintiPerSessoEAnno(@Param("anno") int anno);

        // query per andamento temporale: conteggio per anno/mese di tutte le prenotazioni
        // non annullate, ordinato cronologicamente.
        @Query("""
        SELECT YEAR(p.dataOra), MONTH(p.dataOra), COUNT(p)
        FROM Prenotazione p
        WHERE p.annullata = false
        GROUP BY YEAR(p.dataOra), MONTH(p.dataOra)
        ORDER BY YEAR(p.dataOra), MONTH(p.dataOra)
        """)
        List<Object[]> countPerMese();
}