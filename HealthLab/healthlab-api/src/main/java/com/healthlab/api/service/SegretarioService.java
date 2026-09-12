package com.healthlab.api.service;

import com.healthlab.api.dto.request.SegretarioRegistraPazienteRequest;
import com.healthlab.api.dto.response.EsecutoreDisponibileResponse;
import com.healthlab.api.dto.response.PazienteAnagraficaResponse;
import com.healthlab.api.dto.response.PrenotazioneSegretarioResponse;
import com.healthlab.api.dto.response.RegistrazioneResponse;
import com.healthlab.api.dto.response.StoricoSegretarioItemResponse;
import com.healthlab.api.entity.Prenotazione;
import com.healthlab.api.entity.Segretario;
import com.healthlab.api.repository.PazienteRepository;
import com.healthlab.api.repository.PrenotazioneRepository;
import com.healthlab.api.repository.RefertoRepository;
import com.healthlab.api.security.CurrentUserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SegretarioService {

    private final PazienteRepository pazienteRepository;
    private final PrenotazioneRepository prenotazioneRepository;
    private final RefertoRepository refertoRepository;
    private final AuthService authService;
    private final PrenotazioneService prenotazioneService;
    private final CurrentUserService currentUserService;

    public SegretarioService(PazienteRepository pazienteRepository,
                              PrenotazioneRepository prenotazioneRepository,
                              RefertoRepository refertoRepository,
                              AuthService authService,
                              PrenotazioneService prenotazioneService,
                              CurrentUserService currentUserService) {
        this.pazienteRepository = pazienteRepository;
        this.prenotazioneRepository = prenotazioneRepository;
        this.refertoRepository = refertoRepository;
        this.authService = authService;
        this.prenotazioneService = prenotazioneService;
        this.currentUserService = currentUserService;
    }

    // RF-09: ricerca anagrafica, solo dati identificativi (no dati clinici).
    // Nessun filtro per sede: il paziente non è legato a una sede specifica.
    public List<PazienteAnagraficaResponse> cercaPazienti(String query) {
        String queryPulita = (query == null) ? "" : query.trim();

        return pazienteRepository.ricercaPerNomeCognomeCf(queryPulita).stream()
                .map(p -> new PazienteAnagraficaResponse(
                        p.getId(),
                        p.getUtente().getNome(),
                        p.getUtente().getCognome(),
                        p.getUtente().getCodiceFiscale(),
                        p.getUtente().getTelefono(),
                        p.getUtente().getEmail(),
                        p.getUtente().isEmailVerificata()
                ))
                .toList();
    }

    // Delega ad AuthService, che ha già la logica di creazione Utente+Paziente e invio email
    public RegistrazioneResponse registraPaziente(SegretarioRegistraPazienteRequest request) {
        return authService.registraPerContoSegreteria(request);
    }

    // RF-08: prenotazioni odierne e future della sede del Segretario, con
    // ricerca libera opzionale su nome/cognome/CF del paziente.
    public List<PrenotazioneSegretarioResponse> getPrenotazioni(String query) {
        Segretario segretario = currentUserService.getSegretarioCorrente();
        String queryPulita = (query == null) ? "" : query.trim();

        List<Prenotazione> prenotazioni = prenotazioneRepository
                .findPerSegretario(segretario.getSede().getId(), LocalDateTime.now(), queryPulita);

        return prenotazioni.stream().map(this::toSegretarioResponse).toList();
    }

    private PrenotazioneSegretarioResponse toSegretarioResponse(Prenotazione p) {
        String nomeServizio = nomeServizioDi(p);
        String nomePaziente = nomeCompletoUtente(p.getPaziente().getUtente());
        String cf = p.getPaziente().getUtente().getCodiceFiscale();

        String nomeEsecutore = null;
        String tipoEsecutore = null;
        if (p.getMedico() != null) {
            nomeEsecutore = nomeCompletoUtente(p.getMedico().getUtente());
            tipoEsecutore = "MEDICO";
        } else if (p.getTecnico() != null) {
            nomeEsecutore = nomeCompletoUtente(p.getTecnico().getUtente());
            tipoEsecutore = "TECNICO";
        }

        return new PrenotazioneSegretarioResponse(
                p.getId(), p.getDataOra(), nomePaziente, cf, nomeServizio, nomeEsecutore, tipoEsecutore
        );
    }

    // Storico: prenotazioni passate della sede (incluse le annullate)
    public List<StoricoSegretarioItemResponse> getStorico(String query) {
        Segretario segretario = currentUserService.getSegretarioCorrente();
        String queryPulita = (query == null) ? "" : query.trim();

        List<Prenotazione> prenotazioni = prenotazioneRepository
                .findStoricoPerSegretario(segretario.getSede().getId(), LocalDateTime.now(), queryPulita);

        return prenotazioni.stream()
                .limit(50)
                .map(this::toStoricoResponse)
                .toList();
    }

    private StoricoSegretarioItemResponse toStoricoResponse(Prenotazione p) {
        String nomeServizio = nomeServizioDi(p);
        String nomePaziente = nomeCompletoUtente(p.getPaziente().getUtente());
        String cf = p.getPaziente().getUtente().getCodiceFiscale();

        String nomeEsecutore = null;
        String tipoEsecutore = null;
        if (p.getMedico() != null) {
            nomeEsecutore = nomeCompletoUtente(p.getMedico().getUtente());
            tipoEsecutore = "MEDICO";
        } else if (p.getTecnico() != null) {
            nomeEsecutore = nomeCompletoUtente(p.getTecnico().getUtente());
            tipoEsecutore = "TECNICO";
        }

        String stato;
        if (p.isAnnullata()) {
            stato = "ANNULLATA";
        } else if (refertoRepository.existsByPrenotazione_Id(p.getId())) {
            stato = "REFERTATA";
        } else if (p.getMotivoMancatoReferto() != null) {
            stato = "NON_COMPLETATA";
        } else {
            stato = "SVOLTA";
        }

        return new StoricoSegretarioItemResponse(
                p.getId(), p.getDataOra(), nomePaziente, cf, nomeServizio, nomeEsecutore, tipoEsecutore, stato,
                p.getMotivoMancatoReferto()
        );
    }

    private String nomeServizioDi(Prenotazione p) {
        return p.getPrestazioneDiretta() != null
                ? p.getPrestazioneDiretta().getNome()
                : p.getPrestazionePreviaVisita().getNome();
    }

    private String nomeCompletoUtente(com.healthlab.api.entity.Utente utente) {
        return utente.getNome() + " " + utente.getCognome();
    }

    // Le tre azioni sotto delegano a PrenotazioneService, che già ha tutta la
    // logica di autorizzazione/capacità/anti-race-condition co-locata insieme
    // alle funzioni analoghe già esistenti (sposta del Tecnico, ecc.).

    public void spostaPrenotazione(Integer idPrenotazione, LocalDateTime nuovaDataOra) {
        prenotazioneService.spostaPrenotazioneSegretario(idPrenotazione, nuovaDataOra);
    }

    public List<EsecutoreDisponibileResponse> getEsecutoriDisponibili(Integer idPrenotazione) {
        return prenotazioneService.getEsecutoriDisponibili(idPrenotazione);
    }

    public void riassegnaEsecutore(Integer idPrenotazione, Integer idNuovoEsecutore) {
        prenotazioneService.riassegnaEsecutore(idPrenotazione, idNuovoEsecutore);
    }
}