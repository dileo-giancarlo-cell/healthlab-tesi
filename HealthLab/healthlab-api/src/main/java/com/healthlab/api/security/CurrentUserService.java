package com.healthlab.api.security;

import com.healthlab.api.entity.Medico;
import com.healthlab.api.entity.Segretario;
import com.healthlab.api.entity.Tecnico;
import com.healthlab.api.entity.Utente;
import com.healthlab.api.repository.MedicoRepository;
import com.healthlab.api.repository.SegretarioRepository;
import com.healthlab.api.repository.TecnicoRepository;
import com.healthlab.api.repository.UtenteRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final UtenteRepository utenteRepository;
    private final TecnicoRepository tecnicoRepository;
    private final SegretarioRepository segretarioRepository;
    private final MedicoRepository medicoRepository;

    public CurrentUserService(UtenteRepository utenteRepository,
                               TecnicoRepository tecnicoRepository,
                               SegretarioRepository segretarioRepository,
                               MedicoRepository medicoRepository) {
        this.utenteRepository = utenteRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.segretarioRepository = segretarioRepository;
        this.medicoRepository = medicoRepository;
    }

    public Utente getUtenteCorrente() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return utenteRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Utente autenticato non trovato"));
    }

    public Tecnico getTecnicoCorrente() {
        Utente utente = getUtenteCorrente();
        return tecnicoRepository.findById(utente.getId())
                .orElseThrow(() -> new IllegalArgumentException("L'utente autenticato non è un tecnico"));
    }

    public Segretario getSegretarioCorrente() {
        Utente utente = getUtenteCorrente();
        return segretarioRepository.findById(utente.getId())
                .orElseThrow(() -> new IllegalArgumentException("L'utente autenticato non è un segretario"));
    }

    public Medico getMedicoCorrente() {
        Utente utente = getUtenteCorrente();
        return medicoRepository.findById(utente.getId())
                .orElseThrow(() -> new IllegalArgumentException("L'utente autenticato non è un medico"));
    }
}