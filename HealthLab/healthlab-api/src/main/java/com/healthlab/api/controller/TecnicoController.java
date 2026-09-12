package com.healthlab.api.controller;

import com.healthlab.api.dto.request.ChiudiSenzaRefertoRequest;
import com.healthlab.api.dto.request.SpostaPrenotazioneRequest;
import com.healthlab.api.dto.response.AgendaTecnicoItemResponse;
import com.healthlab.api.dto.response.DaRefertareItemResponse;
import com.healthlab.api.dto.response.GiorniConPrenotazioniResponse;
import com.healthlab.api.dto.response.NomeCognomeResponse;
import com.healthlab.api.dto.response.StoricoTecnicoItemResponse;
import com.healthlab.api.security.CurrentUserService;
import com.healthlab.api.service.TecnicoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/tecnico")
public class TecnicoController {

    private final TecnicoService tecnicoService;
    private final CurrentUserService currentUserService;

    public TecnicoController(TecnicoService tecnicoService, CurrentUserService currentUserService) {
        this.tecnicoService = tecnicoService;
        this.currentUserService = currentUserService;
    }

    // saluto personalizzato in home-interna.html
    @GetMapping("/me")
    public NomeCognomeResponse getMe() {
        var tecnico = currentUserService.getTecnicoCorrente();
        return new NomeCognomeResponse(tecnico.getUtente().getNome(), tecnico.getUtente().getCognome());
    }

    // RF-11: agenda del giorno per il tecnico autenticato.
    // "data" opzionale in formato ISO (YYYY-MM-DD); default: oggi.
    @GetMapping("/agenda")
    public List<AgendaTecnicoItemResponse> getAgenda(
            @RequestParam(required = false) String data) {
        LocalDate giorno = (data != null) ? LocalDate.parse(data) : LocalDate.now();
        return tecnicoService.getAgendaGiorno(giorno);
    }

    // Conteggio prenotazioni per giorno del mese, per il badge nel calendario.
    // "anno"/"mese" opzionali; default: mese corrente.
    @GetMapping("/agenda/giorni-con-prenotazioni")
    public GiorniConPrenotazioniResponse getGiorniConPrenotazioni(
            @RequestParam(required = false) Integer anno,
            @RequestParam(required = false) Integer mese) {
        YearMonth meseAnno = (anno != null && mese != null)
                ? YearMonth.of(anno, mese)
                : YearMonth.now();
        return new GiorniConPrenotazioniResponse(tecnicoService.getConteggioPerGiorno(meseAnno));
    }

    // RF-12: prenotazioni assegnate al tecnico ancora senza referto caricato.
    @GetMapping("/da-refertare")
    public List<DaRefertareItemResponse> getDaRefertare() {
        return tecnicoService.getDaRefertare();
    }

    // Storico completo (sola consultazione) delle prenotazioni passate del tecnico.
    @GetMapping("/storico")
    public List<StoricoTecnicoItemResponse> getStorico() {
        return tecnicoService.getStorico();
    }

    // Sposta una prenotazione futura a un nuovo orario (sezione su agenda/calendario).
    @PatchMapping("/prenotazioni/{id}/sposta")
    public void spostaPrenotazione(@PathVariable Integer id, @RequestBody SpostaPrenotazioneRequest request) {
        tecnicoService.spostaPrenotazione(id, request.getNuovaDataOra());
    }

    // Fasce occupate per un dato giorno, ai fini dello spostamento di questa
    // specifica prenotazione (usato dal modale "Sposta" nel frontend).
    @GetMapping("/prenotazioni/{id}/orari-occupati")
    public List<String> getOrariOccupatiPerSpostamento(
            @PathVariable Integer id,
            @RequestParam String data) {
        return tecnicoService.getOrariOccupatiPerSpostamento(id, LocalDate.parse(data));
    }

    // Chiude una prenotazione passata senza referto, con motivazione obbligatoria
    // (sezione "Da refertare").
    @PatchMapping("/prenotazioni/{id}/chiudi-senza-referto")
    public void chiudiSenzaReferto(@PathVariable Integer id, @RequestBody ChiudiSenzaRefertoRequest request) {
        tecnicoService.chiudiSenzaReferto(id, request.getMotivo());
    }
}