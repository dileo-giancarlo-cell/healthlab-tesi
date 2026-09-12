package com.healthlab.api.service;

import com.healthlab.api.dto.request.InserisciFerieRequest;
import com.healthlab.api.dto.response.FerieResponse;
import com.healthlab.api.dto.response.RisorsaConFerieResponse;
import com.healthlab.api.entity.FerieMedico;
import com.healthlab.api.entity.FerieTecnico;
import com.healthlab.api.entity.Medico;
import com.healthlab.api.entity.Segretario;
import com.healthlab.api.entity.Tecnico;
import com.healthlab.api.exception.ConflittoPrenotazioneException;
import com.healthlab.api.repository.FerieMedicoRepository;
import com.healthlab.api.repository.FerieTecnicoRepository;
import com.healthlab.api.repository.MedicoRepository;
import com.healthlab.api.repository.TecnicoRepository;
import com.healthlab.api.security.CurrentUserService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FerieService {

    private final MedicoRepository medicoRepository;
    private final TecnicoRepository tecnicoRepository;
    private final FerieMedicoRepository ferieMedicoRepository;
    private final FerieTecnicoRepository ferieTecnicoRepository;
    private final CurrentUserService currentUserService;

    public FerieService(MedicoRepository medicoRepository,
                         TecnicoRepository tecnicoRepository,
                         FerieMedicoRepository ferieMedicoRepository,
                         FerieTecnicoRepository ferieTecnicoRepository,
                         CurrentUserService currentUserService) {
        this.medicoRepository = medicoRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.ferieMedicoRepository = ferieMedicoRepository;
        this.ferieTecnicoRepository = ferieTecnicoRepository;
        this.currentUserService = currentUserService;
    }


    public List<RisorsaConFerieResponse> getRisorseConFerie() {
        Segretario segretario = currentUserService.getSegretarioCorrente();
        Integer idSede = segretario.getSede().getId();

        List<RisorsaConFerieResponse> risultato = new ArrayList<>();

        medicoRepository.findAll().stream()
                .filter(m -> m.getSede().getId().equals(idSede))
                .forEach(m -> {
                    List<FerieResponse> ferie = ferieMedicoRepository.findByMedico_IdOrderByDataInizioDesc(m.getId()).stream()
                            .map(f -> new FerieResponse(f.getId(), f.getDataInizio(), f.getDataFine()))
                            .toList();
                    risultato.add(new RisorsaConFerieResponse(
                            m.getId(),
                            "Dott. " + m.getUtente().getNome() + " " + m.getUtente().getCognome(),
                            "MEDICO",
                            ferie));
                });

        tecnicoRepository.findAll().stream()
                .filter(t -> t.getSede().getId().equals(idSede))
                .forEach(t -> {
                    List<FerieResponse> ferie = ferieTecnicoRepository.findByTecnico_IdOrderByDataInizioDesc(t.getId()).stream()
                            .map(f -> new FerieResponse(f.getId(), f.getDataInizio(), f.getDataFine()))
                            .toList();
                    risultato.add(new RisorsaConFerieResponse(
                            t.getId(),
                            t.getUtente().getNome() + " " + t.getUtente().getCognome(),
                            "TECNICO",
                            ferie));
                });

        return risultato;
    }

    public void inserisciFerieMedico(Integer idMedico, InserisciFerieRequest request) {
        Segretario segretario = currentUserService.getSegretarioCorrente();
        Medico medico = medicoRepository.findById(idMedico)
                .orElseThrow(() -> new IllegalArgumentException("Medico non trovato"));

        if (!medico.getSede().getId().equals(segretario.getSede().getId())) {
            throw new IllegalArgumentException("Non sei autorizzato a gestire le ferie di questa risorsa");
        }

        validaIntervallo(request);

        LocalDateTime inizio = request.getDataInizio().atStartOfDay();
        LocalDateTime fineEsclusiva = request.getDataFine().plusDays(1).atStartOfDay();

        long conflitti = ferieMedicoRepository.contaPrenotazioniAttiveNelPeriodo(idMedico, inizio, fineEsclusiva);
        if (conflitti > 0) {
            throw new ConflittoPrenotazioneException(
                    "Ci sono " + conflitti + " prenotazioni attive in questo periodo: spostale o riassegnale prima di inserire le ferie.");
        }

        FerieMedico ferie = new FerieMedico();
        ferie.setMedico(medico);
        ferie.setDataInizio(request.getDataInizio());
        ferie.setDataFine(request.getDataFine());
        ferieMedicoRepository.save(ferie);
    }

    public void inserisciFerieTecnico(Integer idTecnico, InserisciFerieRequest request) {
        Segretario segretario = currentUserService.getSegretarioCorrente();
        Tecnico tecnico = tecnicoRepository.findById(idTecnico)
                .orElseThrow(() -> new IllegalArgumentException("Tecnico non trovato"));

        if (!tecnico.getSede().getId().equals(segretario.getSede().getId())) {
            throw new IllegalArgumentException("Non sei autorizzato a gestire le ferie di questa risorsa");
        }

        validaIntervallo(request);

        LocalDateTime inizio = request.getDataInizio().atStartOfDay();
        LocalDateTime fineEsclusiva = request.getDataFine().plusDays(1).atStartOfDay();

        long conflitti = ferieTecnicoRepository.contaPrenotazioniAttiveNelPeriodo(idTecnico, inizio, fineEsclusiva);
        if (conflitti > 0) {
            throw new ConflittoPrenotazioneException(
                    "Ci sono " + conflitti + " prenotazioni attive in questo periodo: spostale o riassegnale prima di inserire le ferie.");
        }

        FerieTecnico ferie = new FerieTecnico();
        ferie.setTecnico(tecnico);
        ferie.setDataInizio(request.getDataInizio());
        ferie.setDataFine(request.getDataFine());
        ferieTecnicoRepository.save(ferie);
    }

    private void validaIntervallo(InserisciFerieRequest request) {
        if (request.getDataInizio() == null || request.getDataFine() == null) {
            throw new IllegalArgumentException("Data inizio e data fine sono obbligatorie");
        }
        if (request.getDataFine().isBefore(request.getDataInizio())) {
            throw new IllegalArgumentException("La data fine non può precedere la data inizio");
        }
        if (request.getDataInizio().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Non è possibile inserire ferie nel passato");
        }
    }

    public void rimuoviFerieMedico(Integer idFerie) {
        Segretario segretario = currentUserService.getSegretarioCorrente();
        FerieMedico ferie = ferieMedicoRepository.findById(idFerie)
                .orElseThrow(() -> new IllegalArgumentException("Ferie non trovate"));

        if (!ferie.getMedico().getSede().getId().equals(segretario.getSede().getId())) {
            throw new IllegalArgumentException("Non sei autorizzato a gestire le ferie di questa risorsa");
        }
        ferieMedicoRepository.delete(ferie);
    }

    public void rimuoviFerieTecnico(Integer idFerie) {
        Segretario segretario = currentUserService.getSegretarioCorrente();
        FerieTecnico ferie = ferieTecnicoRepository.findById(idFerie)
                .orElseThrow(() -> new IllegalArgumentException("Ferie non trovate"));

        if (!ferie.getTecnico().getSede().getId().equals(segretario.getSede().getId())) {
            throw new IllegalArgumentException("Non sei autorizzato a gestire le ferie di questa risorsa");
        }
        ferieTecnicoRepository.delete(ferie);
    }
}