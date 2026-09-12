package com.healthlab.api.controller;

import com.healthlab.api.dto.request.InserisciFerieRequest;
import com.healthlab.api.dto.request.RiassegnaEsecutoreRequest;
import com.healthlab.api.dto.request.SegretarioRegistraPazienteRequest;
import com.healthlab.api.dto.request.SpostaPrenotazioneRequest;
import com.healthlab.api.dto.response.EsecutoreDisponibileResponse;
import com.healthlab.api.dto.response.PazienteAnagraficaResponse;
import com.healthlab.api.dto.response.PrenotazioneSegretarioResponse;
import com.healthlab.api.dto.response.RegistrazioneResponse;
import com.healthlab.api.dto.response.RisorsaConFerieResponse;
import com.healthlab.api.dto.response.StoricoSegretarioItemResponse;
import com.healthlab.api.service.FerieService;
import com.healthlab.api.service.SegretarioService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/segretario")
public class SegretarioController {

    private final SegretarioService segretarioService;
    private final FerieService ferieService;

    public SegretarioController(SegretarioService segretarioService, FerieService ferieService) {
        this.segretarioService = segretarioService;
        this.ferieService = ferieService;
    }

    // RF-09: ricerca/elenco pazienti. "query" opzionale. Se vuota mostra tutti.
    @GetMapping("/pazienti")
    public List<PazienteAnagraficaResponse> cercaPazienti(
            @RequestParam(required = false, defaultValue = "") String query) {
        return segretarioService.cercaPazienti(query);
    }

    // "Registra nuovo paziente" (es. richiesta telefonica/sportello).
    @PostMapping("/pazienti")
    public RegistrazioneResponse registraPaziente(
            @Valid @RequestBody SegretarioRegistraPazienteRequest request) {
        return segretarioService.registraPaziente(request);
    }

    // RF-08: prenotazioni future della sede del Segretario, con ricerca opzionale.
    @GetMapping("/prenotazioni")
    public List<PrenotazioneSegretarioResponse> getPrenotazioni(
            @RequestParam(required = false, defaultValue = "") String query) {
        return segretarioService.getPrenotazioni(query);
    }

    // Storico prenotazioni passate della sede,
    @GetMapping("/prenotazioni/storico")
    public List<StoricoSegretarioItemResponse> getStorico(
            @RequestParam(required = false, defaultValue = "") String query) {
        return segretarioService.getStorico(query);
    }

    // L'annullamento riusa l'endpoint già esistente PATCH /api/prenotazioni/{id}/annulla

    @PatchMapping("/prenotazioni/{id}/sposta")
    public void spostaPrenotazione(@PathVariable Integer id, @RequestBody SpostaPrenotazioneRequest request) {
        segretarioService.spostaPrenotazione(id, request.getNuovaDataOra());
    }

    @GetMapping("/prenotazioni/{id}/esecutori-disponibili")
    public List<EsecutoreDisponibileResponse> getEsecutoriDisponibili(@PathVariable Integer id) {
        return segretarioService.getEsecutoriDisponibili(id);
    }

    @PatchMapping("/prenotazioni/{id}/riassegna")
    public void riassegnaEsecutore(@PathVariable Integer id, @RequestBody RiassegnaEsecutoreRequest request) {
        segretarioService.riassegnaEsecutore(id, request.getIdNuovoEsecutore());
    }

    // ---------- Ferie  ----------

    @GetMapping("/ferie")
    public List<RisorsaConFerieResponse> getRisorseConFerie() {
        return ferieService.getRisorseConFerie();
    }

    @PostMapping("/ferie/medico/{idMedico}")
    public void inserisciFerieMedico(@PathVariable Integer idMedico, @RequestBody InserisciFerieRequest request) {
        ferieService.inserisciFerieMedico(idMedico, request);
    }

    @PostMapping("/ferie/tecnico/{idTecnico}")
    public void inserisciFerieTecnico(@PathVariable Integer idTecnico, @RequestBody InserisciFerieRequest request) {
        ferieService.inserisciFerieTecnico(idTecnico, request);
    }

    @DeleteMapping("/ferie/medico/{idFerie}")
    public void rimuoviFerieMedico(@PathVariable Integer idFerie) {
        ferieService.rimuoviFerieMedico(idFerie);
    }

    @DeleteMapping("/ferie/tecnico/{idFerie}")
    public void rimuoviFerieTecnico(@PathVariable Integer idFerie) {
        ferieService.rimuoviFerieTecnico(idFerie);
    }
}