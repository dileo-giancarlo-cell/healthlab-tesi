package com.healthlab.api.service;

import com.healthlab.api.dto.response.RefertoResponse;
import com.healthlab.api.entity.Prenotazione;
import com.healthlab.api.entity.Referto;
import com.healthlab.api.entity.Utente;
import com.healthlab.api.repository.PrenotazioneRepository;
import com.healthlab.api.repository.RefertoRepository;
import com.healthlab.api.security.CurrentUserService;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Service
public class RefertoService {

    private final RefertoRepository refertoRepository;
    private final CurrentUserService currentUserService;
    private final FileStorageService fileStorageService;
    private final PrenotazioneRepository prenotazioneRepository;

    public RefertoService(RefertoRepository refertoRepository,
                        CurrentUserService currentUserService,
                        FileStorageService fileStorageService,
                        PrenotazioneRepository prenotazioneRepository) {
        this.refertoRepository = refertoRepository;
        this.currentUserService = currentUserService;
        this.fileStorageService = fileStorageService;
        this.prenotazioneRepository = prenotazioneRepository;
    }

    public RefertoResponse caricaReferto(Integer idPrenotazione, MultipartFile file) {
        Utente utenteCorrente = currentUserService.getUtenteCorrente();
        String ruolo = utenteCorrente.getRuolo().getNome();

        Prenotazione prenotazione = prenotazioneRepository.findById(idPrenotazione)
                .orElseThrow(() -> new IllegalArgumentException("Prenotazione non trovata"));

        boolean autorizzato;
        if ("tecnico".equals(ruolo)) {
            autorizzato = prenotazione.getTecnico() != null
                    && prenotazione.getTecnico().getId().equals(utenteCorrente.getId());
        } else if ("medico".equals(ruolo)) {
            autorizzato = prenotazione.getMedico() != null
                    && prenotazione.getMedico().getId().equals(utenteCorrente.getId());
        } else {
            autorizzato = false;
        }

        if (!autorizzato) {
            throw new IllegalArgumentException(
                    "Non sei autorizzato a caricare il referto per questa prenotazione");
        }

        String nomeFile = fileStorageService.salvaPdf(file);

        Referto referto = new Referto();
        referto.setPrenotazione(prenotazione);
        referto.setFileOValore(nomeFile);
        referto.setDataCaricamento(java.time.LocalDateTime.now());

        Referto salvato = refertoRepository.save(referto);

        String nomeServizio = prenotazione.getPrestazioneDiretta() != null
                ? prenotazione.getPrestazioneDiretta().getNome()
                : prenotazione.getPrestazionePreviaVisita().getNome();

        return new RefertoResponse(salvato.getId(), nomeServizio, salvato.getDataCaricamento(), salvato.getFileOValore());
    }


    public List<RefertoResponse> getRefertiMiei() {
        Utente utenteCorrente = currentUserService.getUtenteCorrente();

        return refertoRepository.findByPrenotazione_Paziente_IdOrderByDataCaricamentoDesc(utenteCorrente.getId()).stream()
                .map(r -> {
                    String nomeServizio = r.getPrenotazione().getPrestazioneDiretta() != null
                            ? r.getPrenotazione().getPrestazioneDiretta().getNome()
                            : r.getPrenotazione().getPrestazionePreviaVisita().getNome();
                    return new RefertoResponse(r.getId(), nomeServizio, r.getDataCaricamento(), r.getFileOValore());
                })
                .toList();
    }

    public ResponseEntity<Resource> scaricaReferto(Integer idReferto) throws IOException {
        Utente utenteCorrente = currentUserService.getUtenteCorrente();

        Referto referto = refertoRepository.findById(idReferto)
                .orElseThrow(() -> new IllegalArgumentException("Referto non trovato"));

        // Controllo di sicurezza: il paziente può scaricare solo i propri referti
        Integer idPazienteReferto = referto.getPrenotazione().getPaziente().getId();
        if (!idPazienteReferto.equals(utenteCorrente.getId())) {
            throw new IllegalArgumentException("Non sei autorizzato a scaricare questo referto");
        }

        Path percorso = fileStorageService.getPercorsoCompleto(referto.getFileOValore());
        Resource risorsa = new UrlResource(percorso.toUri());

        if (!risorsa.exists()) {
            throw new IllegalArgumentException("File non trovato sul server");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"referto.pdf\"")
                .body(risorsa);
    }
}