package com.healthlab.api.service;

import com.healthlab.api.dto.response.ServizioSedeResponse;
import com.healthlab.api.entity.*;
import com.healthlab.api.repository.*;
import com.healthlab.api.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SedeAdminService {

    private final SedeRepository sedeRepository;
    private final PrestazioneDirettaRepository direttaRepository;
    private final PrestazionePreviaVisitaRepository previaVisitaRepository;
    private final SedePrestazioneDirettaRepository sedeDirettaRepository;
    private final SedePrestazionePreviaVisitaRepository sedePreviaVisitaRepository;
    private final CurrentUserService currentUserService;

    public SedeAdminService(SedeRepository sedeRepository,
                             PrestazioneDirettaRepository direttaRepository,
                             PrestazionePreviaVisitaRepository previaVisitaRepository,
                             SedePrestazioneDirettaRepository sedeDirettaRepository,
                             SedePrestazionePreviaVisitaRepository sedePreviaVisitaRepository,
                             CurrentUserService currentUserService) {
        this.sedeRepository = sedeRepository;
        this.direttaRepository = direttaRepository;
        this.previaVisitaRepository = previaVisitaRepository;
        this.sedeDirettaRepository = sedeDirettaRepository;
        this.sedePreviaVisitaRepository = sedePreviaVisitaRepository;
        this.currentUserService = currentUserService;
    }

    // Elenco di tutti i servizi del catalogo (attivi), ciascuno con il flag
    // "disponibile in questa sede" — la checklist che l'Admin spunta/toglie.
    public List<ServizioSedeResponse> getServiziPerSede(Integer idSede) {
        verificaAdmin();
        sedeRepository.findById(idSede)
                .orElseThrow(() -> new IllegalArgumentException("Sede non trovata"));

        List<ServizioSedeResponse> risultato = new java.util.ArrayList<>();

        for (PrestazioneDiretta p : direttaRepository.findAll()) {
            if (!p.isAttivo()) continue;
            boolean disponibile = sedeDirettaRepository.existsBySede_IdAndPrestazioneDiretta_Id(idSede, p.getId());
            risultato.add(new ServizioSedeResponse(p.getId(), p.getNome(), "diretta",
                    p.getCategoriaPrestazione().getNome(), disponibile));
        }
        for (PrestazionePreviaVisita p : previaVisitaRepository.findAll()) {
            if (!p.isAttivo()) continue;
            boolean disponibile = sedePreviaVisitaRepository.existsBySede_IdAndPrestazionePreviaVisita_Id(idSede, p.getId());
            risultato.add(new ServizioSedeResponse(p.getId(), p.getNome(), "previa_visita",
                    p.getCategoriaPrestazione().getNome(), disponibile));
        }

        return risultato;
    }

    @Transactional
    public void attivaServizioInSede(Integer idSede, String tipo, Integer idServizio) {
        verificaAdmin();
        Sede sede = sedeRepository.findById(idSede)
                .orElseThrow(() -> new IllegalArgumentException("Sede non trovata"));

        if ("diretta".equals(tipo)) {
            if (sedeDirettaRepository.existsBySede_IdAndPrestazioneDiretta_Id(idSede, idServizio)) return;
            PrestazioneDiretta prestazione = direttaRepository.findById(idServizio)
                    .orElseThrow(() -> new IllegalArgumentException("Prestazione non trovata"));
            SedePrestazioneDiretta link = new SedePrestazioneDiretta();
            link.setSede(sede);
            link.setPrestazioneDiretta(prestazione);
            sedeDirettaRepository.save(link);
        } else if ("previa_visita".equals(tipo)) {
            if (sedePreviaVisitaRepository.existsBySede_IdAndPrestazionePreviaVisita_Id(idSede, idServizio)) return;
            PrestazionePreviaVisita prestazione = previaVisitaRepository.findById(idServizio)
                    .orElseThrow(() -> new IllegalArgumentException("Prestazione non trovata"));
            SedePrestazionePreviaVisita link = new SedePrestazionePreviaVisita();
            link.setSede(sede);
            link.setPrestazionePreviaVisita(prestazione);
            sedePreviaVisitaRepository.save(link);
        } else {
            throw new IllegalArgumentException("Tipo servizio non valido: " + tipo);
        }
    }

    @Transactional
    public void disattivaServizioInSede(Integer idSede, String tipo, Integer idServizio) {
        verificaAdmin();

        if ("diretta".equals(tipo)) {
            sedeDirettaRepository.deleteBySede_IdAndPrestazioneDiretta_Id(idSede, idServizio);
        } else if ("previa_visita".equals(tipo)) {
            sedePreviaVisitaRepository.deleteBySede_IdAndPrestazionePreviaVisita_Id(idSede, idServizio);
        } else {
            throw new IllegalArgumentException("Tipo servizio non valido: " + tipo);
        }
    }

    private void verificaAdmin() {
        Utente utenteCorrente = currentUserService.getUtenteCorrente();
        if (!"amministratore".equals(utenteCorrente.getRuolo().getNome())) {
            throw new IllegalArgumentException("Non sei autorizzato a eseguire questa operazione");
        }
    }
}