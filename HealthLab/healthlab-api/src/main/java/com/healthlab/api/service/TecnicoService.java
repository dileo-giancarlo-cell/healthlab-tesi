package com.healthlab.api.service;

import com.healthlab.api.dto.response.AgendaTecnicoItemResponse;
import com.healthlab.api.dto.response.DaRefertareItemResponse;
import com.healthlab.api.dto.response.StoricoTecnicoItemResponse;
import com.healthlab.api.entity.Prenotazione;
import com.healthlab.api.entity.Tecnico;
import com.healthlab.api.exception.ConflittoPrenotazioneException;
import com.healthlab.api.repository.PrenotazioneRepository;
import com.healthlab.api.repository.RefertoRepository;
import com.healthlab.api.repository.TecnicoRepository;
import com.healthlab.api.security.CurrentUserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TecnicoService {

    private final PrenotazioneRepository prenotazioneRepository;
    private final RefertoRepository refertoRepository;
    private final TecnicoRepository tecnicoRepository;
    private final CurrentUserService currentUserService;

    public TecnicoService(PrenotazioneRepository prenotazioneRepository,
                           RefertoRepository refertoRepository,
                           TecnicoRepository tecnicoRepository,
                           CurrentUserService currentUserService) {
        this.prenotazioneRepository = prenotazioneRepository;
        this.refertoRepository = refertoRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.currentUserService = currentUserService;
    }

    public List<AgendaTecnicoItemResponse> getAgendaGiorno(LocalDate data) {
        Tecnico tecnico = currentUserService.getTecnicoCorrente();

        LocalDateTime inizioGiorno = data.atStartOfDay();
        LocalDateTime fineGiorno = data.plusDays(1).atStartOfDay();

        List<Prenotazione> prenotazioni = prenotazioneRepository
                .findAgendaTecnico(tecnico.getId(), inizioGiorno, fineGiorno);

        return prenotazioni.stream()
                .map(p -> new AgendaTecnicoItemResponse(
                        p.getId(),
                        nomeCompletoPaziente(p),
                        nomeServizio(p),
                        p.getDataOra(),
                        refertoRepository.existsByPrenotazione_Id(p.getId()),
                        p.getMotivoMancatoReferto()
                ))
                .collect(Collectors.toList());
    }

    // Conteggio delle prenotazioni per ciascun giorno del mese richiesto,
    // usato dal calendario per il badge "+N" e per evidenziare le celle.
    public Map<Integer, Long> getConteggioPerGiorno(YearMonth meseAnno) {
        Tecnico tecnico = currentUserService.getTecnicoCorrente();

        LocalDateTime inizioMese = meseAnno.atDay(1).atStartOfDay();
        LocalDateTime fineMese = meseAnno.plusMonths(1).atDay(1).atStartOfDay();

        List<LocalDateTime> dateOra = prenotazioneRepository
                .findDataOraPrenotazioniTecnicoNelMese(tecnico.getId(), inizioMese, fineMese);

        return dateOra.stream()
                .collect(Collectors.groupingBy(LocalDateTime::getDayOfMonth, Collectors.counting()));
    }

    // RF-12: elenco delle prenotazioni assegnate al tecnico, già passate, ancora
    // senza referto, ordinate dalla più vecchia alla più recente.
    public List<DaRefertareItemResponse> getDaRefertare() {
        Tecnico tecnico = currentUserService.getTecnicoCorrente();

        List<Prenotazione> prenotazioni = prenotazioneRepository
                .findDaRefertareByTecnico(tecnico.getId(), LocalDateTime.now());

        return prenotazioni.stream()
                .map(p -> new DaRefertareItemResponse(
                        p.getId(),
                        p.getDataOra(),
                        p.getPaziente().getUtente().getCodiceFiscale(),
                        nomeCompletoPaziente(p),
                        nomeServizio(p)
                ))
                .collect(Collectors.toList());
    }

    private String nomeCompletoPaziente(Prenotazione p) {
        var utente = p.getPaziente().getUtente();
        return utente.getNome() + " " + utente.getCognome();
    }

    private String nomeServizio(Prenotazione p) {
        if (p.getPrestazioneDiretta() != null) {
            return p.getPrestazioneDiretta().getNome();
        }
        return p.getPrestazionePreviaVisita().getNome();
    }

    // Sposta una prenotazione a un nuovo orario, solo se il tecnico corrente
    // è effettivamente quello assegnato e il nuovo slot ha capacità disponibile.
    public void spostaPrenotazione(Integer idPrenotazione, LocalDateTime nuovaDataOra) {
        Tecnico tecnico = currentUserService.getTecnicoCorrente();

        Prenotazione prenotazione = prenotazioneRepository.findById(idPrenotazione)
                .orElseThrow(() -> new IllegalArgumentException("Prenotazione non trovata"));

        if (prenotazione.getTecnico() == null || !prenotazione.getTecnico().getId().equals(tecnico.getId())) {
            throw new IllegalArgumentException("Non sei autorizzato a modificare questa prenotazione");
        }

        if (prenotazione.getDataOra().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Non è possibile spostare una prenotazione già passata");
        }

        if (nuovaDataOra == null || nuovaDataOra.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La nuova data deve essere futura");
        }

        // Stesso controllo già presente in PrenotazioneService.creaPrenotazione:
        // il paziente non può finire con due prenotazioni dello stesso servizio
        // nello stesso giorno. Scatta solo se il giorno cambia davvero.
        if (!nuovaDataOra.toLocalDate().equals(prenotazione.getDataOra().toLocalDate())) {
            LocalDateTime inizioGiornoNuovo = nuovaDataOra.toLocalDate().atStartOfDay();
            LocalDateTime fineGiornoNuovo = inizioGiornoNuovo.plusDays(1);

            boolean giaPrenotato = prenotazione.getPrestazioneDiretta() != null
                    ? prenotazioneRepository.existsByPaziente_IdAndPrestazioneDiretta_IdAndDataOraBetweenAndAnnullataFalse(
                            prenotazione.getPaziente().getId(), prenotazione.getPrestazioneDiretta().getId(),
                            inizioGiornoNuovo, fineGiornoNuovo)
                    : prenotazioneRepository.existsByPaziente_IdAndPrestazionePreviaVisita_IdAndDataOraBetweenAndAnnullataFalse(
                            prenotazione.getPaziente().getId(), prenotazione.getPrestazionePreviaVisita().getId(),
                            inizioGiornoNuovo, fineGiornoNuovo);

            if (giaPrenotato) {
                throw new ConflittoPrenotazioneException(
                        "Il paziente ha già una prenotazione per questo servizio in quella giornata.");
            }
        }

        Integer idSpecializzazioneTecnico = idSpecializzazioneTecnicoDi(prenotazione);
        long capacita = tecnicoRepository.countDisponibiliInDataESede(
                idSpecializzazioneTecnico, tecnico.getSede().getId(), nuovaDataOra.toLocalDate());
        long occupati = prenotazioneRepository
                .countPrenotazioniAttiveBySpecializzazioneTecnicoESlot(idSpecializzazioneTecnico, nuovaDataOra);

        if (occupati >= capacita) {
            throw new ConflittoPrenotazioneException("Il nuovo orario scelto non è disponibile.");
        }

        prenotazione.setDataOra(nuovaDataOra);

        // saveAndFlush + cattura del vincolo DB: stessa difesa anti-race-condition
        // usata in PrenotazioneService.creaPrenotazione
        // il controllo di capacità sopra non basta da solo per le richieste concorrenti.
        try {
            prenotazioneRepository.saveAndFlush(prenotazione);
        } catch (DataIntegrityViolationException e) {
            throw new ConflittoPrenotazioneException(
                    "Questo slot è stato appena occupato da un'altra prenotazione. Riprova con un altro orario.");
        }
    }

    // Fasce orarie occupate per il giorno indicato per poi poter spostare la prenotazione
    // ( quest'ultimaviene esclusa dal conteggio)
    public List<String> getOrariOccupatiPerSpostamento(Integer idPrenotazione, LocalDate data) {
        Tecnico tecnico = currentUserService.getTecnicoCorrente();

        Prenotazione prenotazione = prenotazioneRepository.findById(idPrenotazione)
                .orElseThrow(() -> new IllegalArgumentException("Prenotazione non trovata"));

        if (prenotazione.getTecnico() == null || !prenotazione.getTecnico().getId().equals(tecnico.getId())) {
            throw new IllegalArgumentException("Non sei autorizzato a consultare questa prenotazione");
        }

        Integer idSpecializzazioneTecnico = idSpecializzazioneTecnicoDi(prenotazione);
        long capacita = tecnicoRepository.countBySpecializzazione_Id(idSpecializzazioneTecnico);

        List<String> occupati = new ArrayList<>();
        LocalTime ora = LocalTime.of(8, 0);
        while (ora.isBefore(LocalTime.of(18, 0))) {
            LocalDateTime slot = data.atTime(ora);
            long occupatiSlot = prenotazioneRepository
                    .countPrenotazioniAttiveBySpecializzazioneTecnicoESlot(idSpecializzazioneTecnico, slot);

            if (prenotazione.getDataOra().equals(slot)) {
                occupatiSlot = Math.max(0, occupatiSlot - 1);
            }

            if (occupatiSlot >= capacita) {
                occupati.add(ora.toString().substring(0, 5));
            }
            ora = ora.plusMinutes(30);
        }
        return occupati;
    }

    // RF-12: chiude una prenotazione senza referto, con una
    // motivazione testuale obbligatoria (es. paziente non presentato).
    public void chiudiSenzaReferto(Integer idPrenotazione, String motivo) {
        Tecnico tecnico = currentUserService.getTecnicoCorrente();

        Prenotazione prenotazione = prenotazioneRepository.findById(idPrenotazione)
                .orElseThrow(() -> new IllegalArgumentException("Prenotazione non trovata"));

        if (prenotazione.getTecnico() == null || !prenotazione.getTecnico().getId().equals(tecnico.getId())) {
            throw new IllegalArgumentException("Non sei autorizzato a modificare questa prenotazione");
        }

        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("La motivazione è obbligatoria");
        }

        if (refertoRepository.existsByPrenotazione_Id(idPrenotazione)) {
            throw new IllegalArgumentException("Questa prenotazione ha già un referto caricato");
        }

        prenotazione.setMotivoMancatoReferto(motivo);
        prenotazioneRepository.save(prenotazione);
    }

    // Storico completo del Tecnico, con il relativo esito, incluse quelle annullate dal paziente.
    public List<StoricoTecnicoItemResponse> getStorico() {
        Tecnico tecnico = currentUserService.getTecnicoCorrente();

        List<Prenotazione> prenotazioni = prenotazioneRepository
                .findStoricoByTecnico(tecnico.getId(), LocalDateTime.now());

        return prenotazioni.stream()
                .limit(50)
                .map(p -> {
                    String stato;
                    if (refertoRepository.existsByPrenotazione_Id(p.getId())) {
                        stato = "REFERTATA";
                    } else if (p.getMotivoMancatoReferto() != null) {
                        stato = "NON_COMPLETATA";
                    } else if (p.isAnnullata()) {
                        stato = "ANNULLATA";
                    } else {
                        stato = "DA_GESTIRE";
                    }
                    return new StoricoTecnicoItemResponse(
                            p.getId(),
                            p.getPaziente().getUtente().getCodiceFiscale(),
                            nomeCompletoPaziente(p),
                            nomeServizio(p),
                            p.getDataOra(),
                            stato,
                            p.getMotivoMancatoReferto()
                    );
                })
                .collect(Collectors.toList());
    }

    private Integer idSpecializzazioneTecnicoDi(Prenotazione p) {
        if (p.getPrestazioneDiretta() != null) {
            return p.getPrestazioneDiretta().getSpecializzazioneTecnico().getId();
        }
        return p.getPrestazionePreviaVisita().getSpecializzazioneTecnico().getId();
    }
}