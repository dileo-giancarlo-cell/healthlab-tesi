package com.healthlab.api.controller;

import com.healthlab.api.dto.request.EsitoVisitaRequest;
import com.healthlab.api.dto.response.NomeCognomeResponse;
import com.healthlab.api.dto.response.PazienteInCaricoResponse;
import com.healthlab.api.dto.response.PrestazioneAutorizzabileResponse;
import com.healthlab.api.dto.response.VisitaDaGestireItemResponse;
import com.healthlab.api.dto.response.VisitaStoricoItemResponse;
import com.healthlab.api.security.CurrentUserService;
import com.healthlab.api.service.VisitaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/medico")
public class MedicoController {

    private final VisitaService visitaService;
    private final CurrentUserService currentUserService;

    public MedicoController(VisitaService visitaService, CurrentUserService currentUserService) {
        this.visitaService = visitaService;
        this.currentUserService = currentUserService;
    }

    // Self-profile minimo, solo per il saluto personalizzato in home-interna.html
    // ("Dashboard medica del Dott. Nome Cognome") — al login si salva solo il
    // nome, mai il cognome, quindi serve una chiamata dedicata.
    @GetMapping("/me")
    public NomeCognomeResponse getMe() {
        var medico = currentUserService.getMedicoCorrente();
        return new NomeCognomeResponse(medico.getUtente().getNome(), medico.getUtente().getCognome());
    }

    // RF-13: pazienti in carico, con ricerca opzionale
    @GetMapping("/pazienti")
    public List<PazienteInCaricoResponse> getPazientiInCarico(
            @RequestParam(required = false, defaultValue = "") String query) {
        return visitaService.getPazientiInCarico(query);
    }

    // RF-13: storico di un paziente specifico (solo le visite di questo medico)
    @GetMapping("/pazienti/{idPaziente}/storico")
    public List<VisitaStoricoItemResponse> getStoricoPaziente(@PathVariable Integer idPaziente) {
        return visitaService.getStoricoPaziente(idPaziente);
    }

    // RF-14: visite passate ancora senza esito registrato
    @GetMapping("/visite-da-gestire")
    public List<VisitaDaGestireItemResponse> getVisiteDaGestire() {
        return visitaService.getVisiteDaGestire();
    }

    // Prossimi appuntamenti futuri (sola consultazione)
    @GetMapping("/prossimi-appuntamenti")
    public List<VisitaDaGestireItemResponse> getProssimiAppuntamenti() {
        return visitaService.getProssimiAppuntamenti();
    }

    // Prestazioni vincolate autorizzabili a partire da questa visita
    @GetMapping("/prenotazioni/{id}/prestazioni-autorizzabili")
    public List<PrestazioneAutorizzabileResponse> getPrestazioniAutorizzabili(@PathVariable Integer id) {
        return visitaService.getPrestazioniAutorizzabili(id);
    }

    // RF-14: registra l'esito della visita
    @PostMapping("/prenotazioni/{id}/esito")
    public void registraEsito(@PathVariable Integer id, @RequestBody EsitoVisitaRequest request) {
        visitaService.registraEsito(id, request);
    }
}