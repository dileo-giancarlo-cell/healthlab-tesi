package com.healthlab.api.service;

import com.healthlab.api.dto.response.DashboardPazienteResponse;
import com.healthlab.api.dto.response.PrenotazioneResponse;
import com.healthlab.api.entity.Prenotazione;
import com.healthlab.api.entity.Utente;
import com.healthlab.api.repository.PrenotazioneRepository;
import com.healthlab.api.repository.RefertoRepository;
import com.healthlab.api.security.CurrentUserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DashboardService {

    private final CurrentUserService currentUserService;
    private final PrenotazioneRepository prenotazioneRepository;
    private final RefertoRepository refertoRepository;

    public DashboardService(CurrentUserService currentUserService,
                             PrenotazioneRepository prenotazioneRepository,
                             RefertoRepository refertoRepository) {
        this.currentUserService = currentUserService;
        this.prenotazioneRepository = prenotazioneRepository;
        this.refertoRepository = refertoRepository;
    }

    public DashboardPazienteResponse getDashboardPaziente() {
        Utente utenteCorrente = currentUserService.getUtenteCorrente();

        List<Prenotazione> prossime = prenotazioneRepository
                .findProssimePrenotazioni(utenteCorrente.getId(), LocalDateTime.now());

        PrenotazioneResponse prossimaPrenotazione = null;
        if (!prossime.isEmpty()) {
            Prenotazione p = prossime.get(0);
            String nomeServizio = p.getPrestazioneDiretta() != null
                    ? p.getPrestazioneDiretta().getNome()
                    : p.getPrestazionePreviaVisita().getNome();
            prossimaPrenotazione = new PrenotazioneResponse(p.getId(), nomeServizio, p.getDataOra(), p.isAnnullata(), null, null, null);
        }

        long refertiRecenti = refertoRepository.countByPrenotazione_Paziente_IdAndDataCaricamentoAfter(
                utenteCorrente.getId(), LocalDateTime.now().minusDays(7));

        return new DashboardPazienteResponse(utenteCorrente.getNome(), prossimaPrenotazione, (int) refertiRecenti);
    }
}