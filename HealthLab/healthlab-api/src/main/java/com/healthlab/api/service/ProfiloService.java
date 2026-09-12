package com.healthlab.api.service;

import com.healthlab.api.dto.response.ProfiloResponse;
import com.healthlab.api.entity.Paziente;
import com.healthlab.api.entity.Utente;
import com.healthlab.api.repository.PazienteRepository;
import com.healthlab.api.repository.PrenotazioneRepository;
import com.healthlab.api.repository.RefertoRepository;
import com.healthlab.api.security.CurrentUserService;
import org.springframework.stereotype.Service;

@Service
public class ProfiloService {

    private final CurrentUserService currentUserService;
    private final PazienteRepository pazienteRepository;
    private final PrenotazioneRepository prenotazioneRepository;
    private final RefertoRepository refertoRepository;

    public ProfiloService(CurrentUserService currentUserService,
                           PazienteRepository pazienteRepository,
                           PrenotazioneRepository prenotazioneRepository,
                           RefertoRepository refertoRepository) {
        this.currentUserService = currentUserService;
        this.pazienteRepository = pazienteRepository;
        this.prenotazioneRepository = prenotazioneRepository;
        this.refertoRepository = refertoRepository;
    }

    public ProfiloResponse getProfiloMio() {
        Utente utenteCorrente = currentUserService.getUtenteCorrente();

        Paziente paziente = pazienteRepository.findById(utenteCorrente.getId())
                .orElseThrow(() -> new IllegalArgumentException("L'utente corrente non è un paziente"));

        int totalePrenotazioni = prenotazioneRepository.findByPaziente_Id(utenteCorrente.getId()).size();
        int totaleReferti = refertoRepository
                .findByPrenotazione_Paziente_IdOrderByDataCaricamentoDesc(utenteCorrente.getId()).size();

        return new ProfiloResponse(
                utenteCorrente.getNome(),
                utenteCorrente.getCognome(),
                utenteCorrente.getEmail(),
                utenteCorrente.getCodiceFiscale(),
                utenteCorrente.getDataNascita(),
                paziente.getSesso() != null ? paziente.getSesso().toString() : null,
                totalePrenotazioni,
                totaleReferti
        );
    }
}