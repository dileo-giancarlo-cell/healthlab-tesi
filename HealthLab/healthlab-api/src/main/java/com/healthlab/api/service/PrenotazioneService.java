package com.healthlab.api.service;

import com.healthlab.api.dto.request.PrenotazioneRequest;
import com.healthlab.api.dto.response.AutorizzazioneStatusResponse;
import com.healthlab.api.dto.response.EsecutoreDisponibileResponse;
import com.healthlab.api.dto.response.PrenotazioneResponse;
import com.healthlab.api.entity.*;
import com.healthlab.api.exception.AutorizzazioneNonPresenteException;
import com.healthlab.api.exception.ConflittoPrenotazioneException;
import com.healthlab.api.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.healthlab.api.security.CurrentUserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PrenotazioneService {

    private final PrenotazioneRepository prenotazioneRepository;
    private final VisitaRepository visitaRepository;
    private final PazienteRepository pazienteRepository;
    private final MedicoRepository medicoRepository;
    private final TecnicoRepository tecnicoRepository;
    private final PrestazioneDirettaRepository direttaRepository;
    private final PrestazionePreviaVisitaRepository previaVisitaRepository;
    private final SedeRepository sedeRepository;
    private final CurrentUserService currentUserService;
    private final PrestazioneDirettaServiziRepository prestazioneDirettaServiziRepository;
    private final PrenotazioneServizioRepository prenotazioneServizioRepository;
    private final EmailService emailService;

    public PrenotazioneService(PrenotazioneRepository prenotazioneRepository,
                            VisitaRepository visitaRepository,
                            PazienteRepository pazienteRepository,
                            MedicoRepository medicoRepository,
                            TecnicoRepository tecnicoRepository,
                            PrestazioneDirettaRepository direttaRepository,
                            PrestazionePreviaVisitaRepository previaVisitaRepository,
                            SedeRepository sedeRepository,
                            CurrentUserService currentUserService,
                            PrestazioneDirettaServiziRepository prestazioneDirettaServiziRepository,
                            PrenotazioneServizioRepository prenotazioneServizioRepository,
                            EmailService emailService) {
        this.prenotazioneRepository = prenotazioneRepository;
        this.visitaRepository = visitaRepository;
        this.pazienteRepository = pazienteRepository;
        this.medicoRepository = medicoRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.direttaRepository = direttaRepository;
        this.previaVisitaRepository = previaVisitaRepository;
        this.sedeRepository = sedeRepository;
        this.currentUserService = currentUserService;
        this.prestazioneDirettaServiziRepository = prestazioneDirettaServiziRepository;
        this.prenotazioneServizioRepository = prenotazioneServizioRepository;
        this.emailService = emailService;
    }

    // Risolve il Paziente per cui vale questa prenotazione: se idPaziente è
    // valorizzato nella request, siamo nel caso "prenota per conto" (RF-10bis
    // per il Segretario, RF-14 per il Medico che sceglie di prenotare lui
    // stesso dopo aver autorizzato) — permesso solo a questi ruoli
    // Altrimenti, la prenotazione riguarda l'utente autenticato stesso.
    private Paziente risolviPazienteDestinatario(Utente utenteCorrente, Integer idPazienteRichiesto) {
        if (idPazienteRichiesto != null) {
            boolean autorizzato = List.of("segretario", "medico").contains(utenteCorrente.getRuolo().getNome());
            if (!autorizzato) {
                throw new IllegalArgumentException("Non sei autorizzato a prenotare per conto di un altro paziente");
            }
            return pazienteRepository.findById(idPazienteRichiesto)
                    .orElseThrow(() -> new IllegalArgumentException("Paziente non trovato"));
        }
        return pazienteRepository.findById(utenteCorrente.getId())
                .orElseThrow(() -> new IllegalArgumentException("L'utente autenticato non è un paziente"));
    }

    // Risolve la SEDE per cui vale la prenotazione. Per Segretario e
    // Medico è sempre e comunque la loro sede di appartenenza — non ha senso
    // che scelgano una sede diversa da dove lavorano, e semplifica il
    // frontend (nessun selettore sede). Solo il Paziente deve
    // scegliere esplicitamente (dropdown in prenotazioni.html): per lui
    // idSedeRichiesta è obbligatorio.
    private Sede risolviSedeEffettiva(Utente utenteCorrente, Integer idSedeRichiesta) {
        String ruolo = utenteCorrente.getRuolo().getNome();

        if ("segretario".equals(ruolo)) {
            return currentUserService.getSegretarioCorrente().getSede();
        }
        if ("medico".equals(ruolo)) {
            return currentUserService.getMedicoCorrente().getSede();
        }

        if (idSedeRichiesta == null) {
            throw new IllegalArgumentException("È necessario specificare una sede per la prenotazione.");
        }
        return sedeRepository.findById(idSedeRichiesta)
                .orElseThrow(() -> new IllegalArgumentException("Sede non trovata"));
    }

    public PrenotazioneResponse creaPrenotazione(PrenotazioneRequest request) {

        boolean isDiretta = request.getIdPrestazioneDiretta() != null;
        boolean isPreviaVisita = request.getIdPrestazionePreviaVisita() != null;

        // Vincolo di dominio: solo una delle due, mai entrambe o nessuna
        // (stesso CHECK già presente a livello di database, qui lo anticipiamo in Java
        // per dare un errore chiaro prima ancora di tentare il salvataggio)
        if (isDiretta == isPreviaVisita) {
            throw new IllegalArgumentException(
                    "La prenotazione deve riguardare esattamente un servizio, diretto o vincolato.");
        }

        // Ulteriore controllo difensivo: il frontend non permette di scegliere uno slot
        // già passato, ma nulla vieta a qualcuno di chiamare l'API direttamente
        // con un orario nel passato — lo blocchiamo qui.
        if (request.getDataOra().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Non è possibile prenotare per un orario già passato.");
        }

        Utente utenteCorrente = currentUserService.getUtenteCorrente();
        Paziente paziente = risolviPazienteDestinatario(utenteCorrente, request.getIdPaziente());
        Sede sedeEffettiva = risolviSedeEffettiva(utenteCorrente, request.getIdSede());

        Prenotazione prenotazione = new Prenotazione();
        prenotazione.setPaziente(paziente);
        prenotazione.setDataOra(request.getDataOra());
        prenotazione.setAnnullata(false);

        String nomeServizio;

        // Tracciano la specializzazione coinvolta e chi esegue la prestazione,
        // per l'assegnazione automatica del Tecnico in basso (solo caso diretto:
        // le prestazioni "previa visita" restano legate a un Medico come prima).
        SpecializzazioneMedico specMedicoEsecutore = null;
        SpecializzazioneTecnico specTecnicoEsecutore = null;

        // Nomi dei parametri selezionati (es. "Colesterolo"), popolata in basso
        // durante il ciclo su idServiziSelezionati — usata solo per l'email di conferma.
        List<String> nomiParametriSelezionati = new ArrayList<>();

        if (isDiretta) {
            // PRIMO CASO: servizio ad accesso diretto (RF-03 / RF-10bis)
            // Nessun controllo di autorizzazione necessario
            // prenotabile in autonomia dal paziente, o per suo conto dal Segretario.
            PrestazioneDiretta prestazione = direttaRepository.findById(request.getIdPrestazioneDiretta())
                    .orElseThrow(() -> new IllegalArgumentException("Prestazione diretta non trovata"));

            // RF-17: controllo se una prestazione è stata disabilitata o meno:
            // Necessario altrimenti basterebbe chiamare direttamente l'API per aggirarla.
            if (!prestazione.isAttivo() || !prestazione.getCategoriaPrestazione().isAttivo()) {
                throw new IllegalArgumentException("Questo servizio non è al momento disponibile.");
            }

                LocalDateTime inizioGiorno = request.getDataOra().toLocalDate().atStartOfDay();
                LocalDateTime fineGiorno = inizioGiorno.plusDays(1);

                // Controllo anti-doppione sul paziente destinatario, non su chi sta
                // effettuando la chiamata (importante quando prenota il Segretario).
                if (prenotazioneRepository.existsByPaziente_IdAndPrestazioneDiretta_IdAndDataOraBetweenAndAnnullataFalse(
                        paziente.getId(), request.getIdPrestazioneDiretta(), inizioGiorno, fineGiorno)) {
                throw new ConflittoPrenotazioneException("Hai già una prenotazione per questo servizio in questa giornata.");
                }

                Integer idSpecializzazione = prestazione.getSpecializzazioneMedico() != null
                        ? prestazione.getSpecializzazioneMedico().getId()
                        : prestazione.getSpecializzazioneTecnico().getId();
                boolean eseguitaDaTecnico = prestazione.isEseguitaDaTecnico();

                boolean slotPieno = eseguitaDaTecnico
                        ? capacitaTecnicoPienaInSlot(idSpecializzazione, sedeEffettiva.getId(), request.getDataOra())
                        : specializzazionePienaInSlot(idSpecializzazione, sedeEffettiva.getId(), request.getDataOra());

                if (slotPieno) {
                throw new ConflittoPrenotazioneException("Questo slot non è più disponibile, scegline un altro.");
                }
                prenotazione.setPrestazioneDiretta(prestazione);
                nomeServizio = prestazione.getNome();
                specMedicoEsecutore = prestazione.getSpecializzazioneMedico();
                specTecnicoEsecutore = prestazione.getSpecializzazioneTecnico();

        } else {
            // SECONDO CASO: servizio vincolato (RF-04 / RF-10bis) 
            // L'autorizzazione va verificata sul paziente, non su
            // chi sta effettuando la chiamata ( importante per il
            // Segretario: se il paziente non ha un'autorizzazione attiva, deve
            // prenotare la visita propedeutica anziché la prestazione vincolata
            PrestazionePreviaVisita prestazione = previaVisitaRepository.findById(request.getIdPrestazionePreviaVisita())
                    .orElseThrow(() -> new IllegalArgumentException("Prestazione vincolata non trovata"));

            // RF-17: stessa difesa del caso diretto — vedi commento sopra.
            if (!prestazione.isAttivo() || !prestazione.getCategoriaPrestazione().isAttivo()) {
                throw new IllegalArgumentException("Questo servizio non è al momento disponibile.");
            }

            boolean autorizzato = visitaRepository.esisteAutorizzazioneAttiva(
                    paziente.getId(), request.getIdPrestazionePreviaVisita());

            if (!autorizzato) {
                throw new AutorizzazioneNonPresenteException(
                        "Nessuna autorizzazione attiva per questa prestazione: prenotare prima la visita di valutazione ("
                        + prestazione.getVisitaPropedeutica().getNome() + ").");
            }

                LocalDateTime inizioGiorno = request.getDataOra().toLocalDate().atStartOfDay();
                LocalDateTime fineGiorno = inizioGiorno.plusDays(1);

                if (prenotazioneRepository.existsByPaziente_IdAndPrestazionePreviaVisita_IdAndDataOraBetweenAndAnnullataFalse(
                        paziente.getId(), request.getIdPrestazionePreviaVisita(), inizioGiorno, fineGiorno)) {
                throw new ConflittoPrenotazioneException("Hai già una prenotazione per questo servizio in questa giornata.");
                }

                // Nota: la capacità dello slot riguarda CHI ESEGUE questa prestazione vincolata
                // (prestazione.getSpecializzazioneMedico()/Tecnico()), e non la specializzazione
                // della visita propedeutica che l'ha autorizzata — le due possono differire
                // (es. Radiografia sbloccata da una visita ortopedica ma eseguita da un tecnico di
                // radiologia).
                if (slotPienoPerEsecutore(prestazione.getSpecializzazioneMedico(),
                        prestazione.getSpecializzazioneTecnico(), sedeEffettiva.getId(), request.getDataOra())) {
                throw new ConflittoPrenotazioneException("Questo slot non è più disponibile, scegline un altro.");
                }
                prenotazione.setPrestazionePreviaVisita(prestazione);
                nomeServizio = prestazione.getNome();
                specMedicoEsecutore = prestazione.getSpecializzazioneMedico();
                specTecnicoEsecutore = prestazione.getSpecializzazioneTecnico();
        }

        assegnaEsecutore(prenotazione, specMedicoEsecutore, specTecnicoEsecutore, sedeEffettiva.getId(), request.getDataOra(), request);

        // perchè saveAndFlush: forza la scrittura immediata, così un'eventuale
        // violazione del vincolo UNIQUE anti-race-condition
        // viene intercettata subito qui, invece che più avanti
        Prenotazione salvata;
        try {
            salvata = prenotazioneRepository.saveAndFlush(prenotazione);
        } catch (DataIntegrityViolationException e) {
            throw new ConflittoPrenotazioneException(
                    "Questo slot è stato appena occupato da un'altra prenotazione. Riprova con un altro orario.");
        }

        if (isPreviaVisita) {
            List<Visita> autorizzazioniAttive = visitaRepository.findAutorizzazioniAttive(
                    paziente.getId(), request.getIdPrestazionePreviaVisita());
            autorizzazioniAttive.forEach(v -> v.setAutorizzazioneConsumata(true));
            visitaRepository.saveAll(autorizzazioniAttive);
        }

        if (request.getIdServiziSelezionati() != null && !request.getIdServiziSelezionati().isEmpty()) {
        for (Integer idServizio : request.getIdServiziSelezionati()) {
                PrenotazioneServizio ps = new PrenotazioneServizio();
                ps.setPrenotazione(salvata);
                // il servizio va recuperato dal repository, per collegarlo correttamente
                PrestazioneDirettaServizi servizio = prestazioneDirettaServiziRepository.findById(idServizio)
                        .orElseThrow(() -> new IllegalArgumentException("Parametro non valido: " + idServizio));
                ps.setServizio(servizio);
                prenotazioneServizioRepository.save(ps);
                nomiParametriSelezionati.add(servizio.getNome());
        }
        }

        inviaEmailConfermaSicura(salvata, paziente, nomeServizio, nomiParametriSelezionati);

        return new PrenotazioneResponse(
                salvata.getId(), nomeServizio, salvata.getDataOra(), salvata.isAnnullata(), null, null, null);
    }

    
    private void inviaEmailConfermaSicura(Prenotazione salvata, Paziente paziente,
                                           String nomeServizio, List<String> parametriSelezionati) {
        try {
            String nomeSede = null;
            String indirizzoSede = null;
            String nomeMedico = null;

            if (salvata.getMedico() != null) {
                Sede sede = salvata.getMedico().getSede();
                nomeSede = sede.getNome();
                indirizzoSede = sede.getIndirizzo();
                nomeMedico = salvata.getMedico().getUtente().getNome() + " " + salvata.getMedico().getUtente().getCognome();
            } else if (salvata.getTecnico() != null) {
                Sede sede = salvata.getTecnico().getSede();
                nomeSede = sede.getNome();
                indirizzoSede = sede.getIndirizzo();
            }

            Utente utentePaziente = paziente.getUtente();

            emailService.inviaEmailConfermaPrenotazione(
                    utentePaziente.getEmail(),
                    utentePaziente.getNome(),
                    nomeServizio,
                    salvata.getDataOra(),
                    nomeSede,
                    indirizzoSede,
                    nomeMedico,
                    parametriSelezionati
            );
        } catch (Exception e) {
            System.err.println("Errore nell'invio email di conferma prenotazione: " + e.getMessage());
        }
    }

    // idPazienteRichiesto: valorizzato SOLO quando è il Segretario o il Medico
    // a verificare l'autorizzazione di un paziente (RF-10bis /
    // RF-14). Se null, si comporta come sempre (verifica sull'utente autenticato)
    public AutorizzazioneStatusResponse verificaAutorizzazione(
            Integer idPrestazionePreviaVisita, Integer idPazienteRichiesto) {
        Utente utenteCorrente = currentUserService.getUtenteCorrente();

        Integer idPazienteEffettivo;
        if (idPazienteRichiesto != null) {
            boolean autorizzato = List.of("segretario", "medico").contains(utenteCorrente.getRuolo().getNome());
            if (!autorizzato) {
                throw new IllegalArgumentException("Non sei autorizzato a consultare l'autorizzazione di un altro paziente");
            }
            idPazienteEffettivo = idPazienteRichiesto;
        } else {
            idPazienteEffettivo = utenteCorrente.getId();
        }

        PrestazionePreviaVisita prestazione = previaVisitaRepository.findById(idPrestazionePreviaVisita)
                .orElseThrow(() -> new IllegalArgumentException("Prestazione vincolata non trovata"));

        boolean autorizzata = visitaRepository.esisteAutorizzazioneAttiva(idPazienteEffettivo, idPrestazionePreviaVisita);

        return new AutorizzazioneStatusResponse(
                autorizzata,
                prestazione.getVisitaPropedeutica().getId(),
                prestazione.getVisitaPropedeutica().getNome()
        );
        }       

    public List<PrenotazioneResponse> getStoricoPaziente() {
        Utente utenteCorrente = currentUserService.getUtenteCorrente();
        return prenotazioneRepository.findByPaziente_Id(utenteCorrente.getId()).stream()
                .map(this::toResponse)
                .toList();
        }


    private PrenotazioneResponse toResponse(Prenotazione p) {
        String nome = p.getPrestazioneDiretta() != null
                ? p.getPrestazioneDiretta().getNome()
                : p.getPrestazionePreviaVisita().getNome();

        Boolean visitaConclusa = null;
        String nomePrestazioneAutorizzata = null;
        String noteVisita = null;

        java.util.Optional<Visita> visitaOpt = visitaRepository.findByPrenotazione_Id(p.getId());
        if (visitaOpt.isPresent()) {
            Visita visita = visitaOpt.get();
            visitaConclusa = visita.isConclusa();
          
            if (visita.isConclusa()
                    && visita.getPrestazionePreviaVisitaAutorizzata() != null
                    && !visita.isAutorizzazioneConsumata()) {
                nomePrestazioneAutorizzata = visita.getPrestazionePreviaVisitaAutorizzata().getNome();
            }
            noteVisita = visita.getNote();
        }

        return new PrenotazioneResponse(
                p.getId(), nome, p.getDataOra(), p.isAnnullata(),
                visitaConclusa, nomePrestazioneAutorizzata, noteVisita);
    }

   
    public List<String> getOrariOccupati(Integer idServizio, boolean isDiretta, LocalDate data, Integer idSedeRichiesta) {
        Utente utenteCorrente = currentUserService.getUtenteCorrente();
        Sede sedeEffettiva = risolviSedeEffettiva(utenteCorrente, idSedeRichiesta);

        Integer idSpecializzazione;
        boolean eseguitaDaTecnico;

        if (isDiretta) {
            PrestazioneDiretta prestazione = direttaRepository.findById(idServizio)
                    .orElseThrow(() -> new IllegalArgumentException("Servizio non trovato"));
            eseguitaDaTecnico = prestazione.isEseguitaDaTecnico();
            idSpecializzazione = eseguitaDaTecnico
                    ? prestazione.getSpecializzazioneTecnico().getId()
                    : prestazione.getSpecializzazioneMedico().getId();
        } else {
            PrestazionePreviaVisita prestazione = previaVisitaRepository.findById(idServizio)
                    .orElseThrow(() -> new IllegalArgumentException("Servizio non trovato"));
            eseguitaDaTecnico = prestazione.isEseguitaDaTecnico();
            idSpecializzazione = eseguitaDaTecnico
                    ? prestazione.getSpecializzazioneTecnico().getId()
                    : prestazione.getSpecializzazioneMedico().getId();
        }

        long capacita = eseguitaDaTecnico
                ? tecnicoRepository.countDisponibiliInDataESede(idSpecializzazione, sedeEffettiva.getId(), data)
                : medicoRepository.countDisponibiliInDataESede(idSpecializzazione, sedeEffettiva.getId(), data);

        List<String> occupati = new ArrayList<>();
        LocalTime ora = LocalTime.of(8, 0);
        LocalDateTime adesso = LocalDateTime.now();
        while (ora.isBefore(LocalTime.of(18, 0))) {
                LocalDateTime slot = data.atTime(ora);
                boolean giaPassato = slot.isBefore(adesso);
                long occupatiSlot = eseguitaDaTecnico
                        ? prenotazioneRepository.countPrenotazioniAttiveBySpecializzazioneTecnicoESlot(idSpecializzazione, slot)
                        : prenotazioneRepository.countPrenotazioniAttiveBySpecializzazioneMedicoESlot(idSpecializzazione, slot);
                if (giaPassato || occupatiSlot >= capacita) {
                occupati.add(ora.toString().substring(0, 5));
                }
                ora = ora.plusMinutes(30);
        }
        return occupati;
        }

        public void annullaPrenotazione(Integer idPrenotazione) {
        Utente utenteCorrente = currentUserService.getUtenteCorrente();

        Prenotazione prenotazione = prenotazioneRepository.findById(idPrenotazione)
                .orElseThrow(() -> new IllegalArgumentException("Prenotazione non trovata"));

 
        boolean isProprietario = prenotazione.getPaziente().getId().equals(utenteCorrente.getId());
        boolean isSegretarioAutorizzato = false;

        if (!isProprietario && "segretario".equals(utenteCorrente.getRuolo().getNome())) {
                isSegretarioAutorizzato = prenotazioneNellaSedeDelSegretario(
                        prenotazione, currentUserService.getSegretarioCorrente());
        }

        if (!isProprietario && !isSegretarioAutorizzato) {
                throw new IllegalArgumentException("Non sei autorizzato ad annullare questa prenotazione");
        }

        if (prenotazione.getDataOra().isBefore(java.time.LocalDateTime.now())) {
                throw new ConflittoPrenotazioneException("Non è possibile annullare una prenotazione già passata.");
        }

        prenotazione.setAnnullata(true);
        prenotazioneRepository.save(prenotazione);
        }

        public void spostaPrenotazioneSegretario(Integer idPrenotazione, LocalDateTime nuovaDataOra) {
                var segretario = currentUserService.getSegretarioCorrente();

                Prenotazione prenotazione = prenotazioneRepository.findById(idPrenotazione)
                        .orElseThrow(() -> new IllegalArgumentException("Prenotazione non trovata"));

                if (!prenotazioneNellaSedeDelSegretario(prenotazione, segretario)) {
                        throw new IllegalArgumentException("Non sei autorizzato a gestire questa prenotazione");
                }

                if (prenotazione.getDataOra().isBefore(LocalDateTime.now())) {
                        throw new ConflittoPrenotazioneException("Non è possibile spostare una prenotazione già passata.");
                }
                if (nuovaDataOra == null || nuovaDataOra.isBefore(LocalDateTime.now())) {
                        throw new IllegalArgumentException("La nuova data deve essere futura");
                }

                // Stesso controllo presente in creaPrenotazione: il paziente non
                // può avere due prenotazioni dello stesso servizio nello stesso
                // giorno. Scatta solo se il GIORNO cambia davvero — altrimenti
                // troverebbe sempre se stessa (la riga in DB ha ancora la vecchia
                // dataOra finché non salviamo più sotto).
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

                SpecializzazioneMedico specMedico = specializzazioneMedicoDi(prenotazione);
                SpecializzazioneTecnico specTecnico = specializzazioneTecnicoDi(prenotazione);

                // Stessa sede della prenotazione originale (via medico/tecnico già
                // assegnato) — spostare la data non cambia la sede.
                Integer idSede = prenotazione.getMedico() != null
                        ? prenotazione.getMedico().getSede().getId()
                        : prenotazione.getTecnico().getSede().getId();

                if (slotPienoPerEsecutore(specMedico, specTecnico, idSede, nuovaDataOra)) {
                        throw new ConflittoPrenotazioneException("Il nuovo orario scelto non è disponibile.");
                }

                prenotazione.setDataOra(nuovaDataOra);

                try {
                        prenotazioneRepository.saveAndFlush(prenotazione);
                } catch (DataIntegrityViolationException e) {
                        throw new ConflittoPrenotazioneException(
                                "Questo slot è stato appena occupato da un'altra prenotazione. Riprova con un altro orario.");
                }
        }

        // RF-08: elenco di medici/tecnici (a seconda di chi è già assegnato) liberi
        // nello stesso slot della prenotazione, per la riassegnazione.
        public List<EsecutoreDisponibileResponse> getEsecutoriDisponibili(Integer idPrenotazione) {
                var segretario = currentUserService.getSegretarioCorrente();

                Prenotazione prenotazione = prenotazioneRepository.findById(idPrenotazione)
                        .orElseThrow(() -> new IllegalArgumentException("Prenotazione non trovata"));

                if (!prenotazioneNellaSedeDelSegretario(prenotazione, segretario)) {
                        throw new IllegalArgumentException("Non sei autorizzato a gestire questa prenotazione");
                }

                // Per ora la riassegnazione resta SEMPRE dentro la sede del Segretario.
                Integer idSede = segretario.getSede().getId();

                if (prenotazione.getMedico() != null) {
                        Integer idSpec = prenotazione.getMedico().getSpecializzazione().getId();
                        return medicoRepository.findMediciLiberiInSlotESede(
                                        idSpec, idSede, prenotazione.getDataOra(), prenotazione.getDataOra().toLocalDate()).stream()
                                .map(m -> new EsecutoreDisponibileResponse(
                                        m.getId(), m.getUtente().getNome() + " " + m.getUtente().getCognome()))
                                .toList();
                } else if (prenotazione.getTecnico() != null) {
                        Integer idSpec = prenotazione.getTecnico().getSpecializzazione().getId();
                        return tecnicoRepository.findTecniciLiberiInSlotESede(
                                        idSpec, idSede, prenotazione.getDataOra(), prenotazione.getDataOra().toLocalDate()).stream()
                                .map(t -> new EsecutoreDisponibileResponse(
                                        t.getId(), t.getUtente().getNome() + " " + t.getUtente().getCognome()))
                                .toList();
                }
                return List.of();
        }

        // RF-08: riassegna la prenotazione a un altro medico/tecnico (stesso ruolo
        // di quello già assegnato — non cambia il tipo di esecutore,
        // solo la persona specifica).
        public void riassegnaEsecutore(Integer idPrenotazione, Integer idNuovoEsecutore) {
                var segretario = currentUserService.getSegretarioCorrente();

                Prenotazione prenotazione = prenotazioneRepository.findById(idPrenotazione)
                        .orElseThrow(() -> new IllegalArgumentException("Prenotazione non trovata"));

                if (!prenotazioneNellaSedeDelSegretario(prenotazione, segretario)) {
                        throw new IllegalArgumentException("Non sei autorizzato a gestire questa prenotazione");
                }

                if (prenotazione.getMedico() != null) {
                        Medico nuovoMedico = medicoRepository.findById(idNuovoEsecutore)
                                .orElseThrow(() -> new IllegalArgumentException("Medico non trovato"));
                        // Difesa: il nuovo esecutore deve appartenere alla stessa sede,
                        // altrimenti si riassegnerebbe a qualcuno che non lavora lì.
                        if (!nuovoMedico.getSede().getId().equals(segretario.getSede().getId())) {
                                throw new IllegalArgumentException("Questo medico non lavora nella tua sede");
                        }
                        prenotazione.setMedico(nuovoMedico);
                } else if (prenotazione.getTecnico() != null) {
                        Tecnico nuovoTecnico = tecnicoRepository.findById(idNuovoEsecutore)
                                .orElseThrow(() -> new IllegalArgumentException("Tecnico non trovato"));
                        if (!nuovoTecnico.getSede().getId().equals(segretario.getSede().getId())) {
                                throw new IllegalArgumentException("Questo tecnico non lavora nella tua sede");
                        }
                        prenotazione.setTecnico(nuovoTecnico);
                } else {
                        throw new IllegalArgumentException("Prenotazione senza esecutore assegnato");
                }

                try {
                        prenotazioneRepository.saveAndFlush(prenotazione);
                } catch (DataIntegrityViolationException e) {
                        throw new ConflittoPrenotazioneException(
                                "La risorsa scelta è stata appena assegnata a un'altra prenotazione nello stesso slot. Riprova.");
                }
        }

        private boolean prenotazioneNellaSedeDelSegretario(Prenotazione prenotazione, Segretario segretario) {
                Integer idSede = null;
                if (prenotazione.getMedico() != null) {
                        idSede = prenotazione.getMedico().getSede().getId();
                } else if (prenotazione.getTecnico() != null) {
                        idSede = prenotazione.getTecnico().getSede().getId();
                }
                return idSede != null && idSede.equals(segretario.getSede().getId());
        }

        private SpecializzazioneMedico specializzazioneMedicoDi(Prenotazione p) {
                return p.getPrestazioneDiretta() != null
                        ? p.getPrestazioneDiretta().getSpecializzazioneMedico()
                        : p.getPrestazionePreviaVisita().getSpecializzazioneMedico();
        }

        private SpecializzazioneTecnico specializzazioneTecnicoDi(Prenotazione p) {
                return p.getPrestazioneDiretta() != null
                        ? p.getPrestazioneDiretta().getSpecializzazioneTecnico()
                        : p.getPrestazionePreviaVisita().getSpecializzazioneTecnico();
        }

        private boolean specializzazionePienaInSlot(Integer idSpecializzazione, Integer idSede, LocalDateTime dataOra) {
                long capacita = medicoRepository.countDisponibiliInDataESede(idSpecializzazione, idSede, dataOra.toLocalDate());
                long occupati = prenotazioneRepository.countPrenotazioniAttiveBySpecializzazioneMedicoESlot(idSpecializzazione, dataOra);
                return occupati >= capacita;
        }

        private boolean capacitaTecnicoPienaInSlot(Integer idSpecializzazione, Integer idSede, LocalDateTime dataOra) {
                long capacita = tecnicoRepository.countDisponibiliInDataESede(idSpecializzazione, idSede, dataOra.toLocalDate());
                long occupati = prenotazioneRepository.countPrenotazioniAttiveBySpecializzazioneTecnicoESlot(idSpecializzazione, dataOra);
                return occupati >= capacita;
        }

        // Capacità dello slot in base a CHI esegue la prestazione: esattamente uno
        // tra specMedico/specTecnico è non-null (garantito dal CHECK a livello DB).
        private boolean slotPienoPerEsecutore(SpecializzazioneMedico specMedico,
                                               SpecializzazioneTecnico specTecnico,
                                               Integer idSede,
                                               LocalDateTime dataOra) {
                if (specTecnico != null) {
                        return capacitaTecnicoPienaInSlot(specTecnico.getId(), idSede, dataOra);
                } else {
                        return specializzazionePienaInSlot(specMedico.getId(), idSede, dataOra);
                }
        }

        // Assegna medico/tecnico alla prenotazione, sempre e solo tra il personale
        // della sede utilizzata per questa prenotazione. Se il chiamante specifica
        // esplicitamente un ID (es. il Medico che forza l'assegnazione a sé
        // stesso dopo un'autorizzazione), verifichiamo comunque che appartenga
        // alla sede giusta — altrimenti si potrebbe assegnare a chi non lavora lì.
        private void assegnaEsecutore(Prenotazione prenotazione,
                                       SpecializzazioneMedico specMedico,
                                       SpecializzazioneTecnico specTecnico,
                                       Integer idSede,
                                       LocalDateTime dataOra,
                                       PrenotazioneRequest request) {
                if (request.getIdMedico() != null) {
                        Medico medico = medicoRepository.findById(request.getIdMedico())
                                        .orElseThrow(() -> new IllegalArgumentException("Medico non trovato"));
                        if (!medico.getSede().getId().equals(idSede)) {
                                throw new IllegalArgumentException("Il medico scelto non lavora nella sede della prenotazione.");
                        }
                        prenotazione.setMedico(medico);
                } else if (specMedico != null) {
                        List<Medico> mediciLiberi = medicoRepository
                                        .findMediciLiberiInSlotESede(specMedico.getId(), idSede, dataOra, dataOra.toLocalDate());
                        if (mediciLiberi.isEmpty()) {
                                throw new ConflittoPrenotazioneException("Questo slot non è più disponibile, scegline un altro.");
                        }
                        prenotazione.setMedico(mediciLiberi.get(0));
                }

                if (request.getIdTecnico() != null) {
                        Tecnico tecnico = tecnicoRepository.findById(request.getIdTecnico())
                                        .orElseThrow(() -> new IllegalArgumentException("Tecnico non trovato"));
                        if (!tecnico.getSede().getId().equals(idSede)) {
                                throw new IllegalArgumentException("Il tecnico scelto non lavora nella sede della prenotazione.");
                        }
                        prenotazione.setTecnico(tecnico);
                } else if (specTecnico != null) {
                        List<Tecnico> tecniciLiberi = tecnicoRepository
                                        .findTecniciLiberiInSlotESede(specTecnico.getId(), idSede, dataOra, dataOra.toLocalDate());
                        if (tecniciLiberi.isEmpty()) {
                                // Difesa in profondità: non dovrebbe accadere, la capacità è già stata
                                // verificata sopra, ma previene una race condition tra due prenotazioni
                                // quasi simultanee sullo stesso slot.
                                throw new ConflittoPrenotazioneException("Questo slot non è più disponibile, scegline un altro.");
                        }
                        prenotazione.setTecnico(tecniciLiberi.get(0));
                }
        }
}