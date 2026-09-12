package com.healthlab.api.controller;

import com.healthlab.api.dto.request.PrenotazioneRequest;
import com.healthlab.api.dto.response.AutorizzazioneStatusResponse;
import com.healthlab.api.dto.response.PrenotazioneResponse;
import com.healthlab.api.service.PrenotazioneService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prenotazioni")
public class PrenotazioneController {

    private final PrenotazioneService prenotazioneService;

    public PrenotazioneController(PrenotazioneService prenotazioneService) {
        this.prenotazioneService = prenotazioneService;
    }

    @PostMapping
    public PrenotazioneResponse crea(@Valid @RequestBody PrenotazioneRequest request) {
        return prenotazioneService.creaPrenotazione(request);
    }

    @GetMapping("/me")
    public List<PrenotazioneResponse> storicoMio() {
        return prenotazioneService.getStoricoPaziente();
    }

    // idPaziente opzionale: valorizzato solo quando il Segretario verifica
    // l'autorizzazione di un paziente diverso da sé (RF-10bis).
    @GetMapping("/verifica-autorizzazione")
    public AutorizzazioneStatusResponse verificaAutorizzazione(
            @RequestParam Integer idPrestazionePreviaVisita,
            @RequestParam(required = false) Integer idPaziente) {
        return prenotazioneService.verificaAutorizzazione(idPrestazionePreviaVisita, idPaziente);
    }

    @GetMapping("/orari-occupati")
    public List<String> getOrariOccupati(
            @RequestParam Integer idServizio,
            @RequestParam boolean isDiretta,
            @RequestParam String data,
            @RequestParam(required = false) Integer idSede) {
        return prenotazioneService.getOrariOccupati(idServizio, isDiretta, java.time.LocalDate.parse(data), idSede);
    }

    @PatchMapping("/{id}/annulla")
    public void annulla(@PathVariable Integer id) {
        prenotazioneService.annullaPrenotazione(id);
    }
}