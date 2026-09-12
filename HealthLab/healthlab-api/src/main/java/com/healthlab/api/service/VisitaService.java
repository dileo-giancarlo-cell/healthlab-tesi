package com.healthlab.api.service;

import com.healthlab.api.dto.request.EsitoVisitaRequest;
import com.healthlab.api.dto.response.PazienteInCaricoResponse;
import com.healthlab.api.dto.response.PrestazioneAutorizzabileResponse;
import com.healthlab.api.dto.response.VisitaDaGestireItemResponse;
import com.healthlab.api.dto.response.VisitaStoricoItemResponse;
import com.healthlab.api.entity.Medico;
import com.healthlab.api.entity.Paziente;
import com.healthlab.api.entity.Prenotazione;
import com.healthlab.api.entity.PrestazionePreviaVisita;
import com.healthlab.api.entity.PrestazioneDiretta;
import com.healthlab.api.entity.Visita;
import com.healthlab.api.repository.PrenotazioneRepository;
import com.healthlab.api.repository.PrestazionePreviaVisitaRepository;
import com.healthlab.api.repository.VisitaRepository;
import com.healthlab.api.security.CurrentUserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class VisitaService {

    private final PrenotazioneRepository prenotazioneRepository;
    private final VisitaRepository visitaRepository;
    private final PrestazionePreviaVisitaRepository previaVisitaRepository;
    private final CurrentUserService currentUserService;

    public VisitaService(PrenotazioneRepository prenotazioneRepository,
                          VisitaRepository visitaRepository,
                          PrestazionePreviaVisitaRepository previaVisitaRepository,
                          CurrentUserService currentUserService) {
        this.prenotazioneRepository = prenotazioneRepository;
        this.visitaRepository = visitaRepository;
        this.previaVisitaRepository = previaVisitaRepository;
        this.currentUserService = currentUserService;
    }

    // RF-13: pazienti con cui questo medico ha avuto almeno una visita.
    // Deriva l'elenco dalle Visita già registrate — un paziente compare qui
    // solo se il medico ha già effettivamente concluso/registrato un incontro,
    // mai solo per una prenotazione ancora da gestire.
    public List<PazienteInCaricoResponse> getPazientiInCarico(String query) {
        Medico medico = currentUserService.getMedicoCorrente();
        List<Visita> visite = visitaRepository.findByPrenotazione_Medico_Id(medico.getId());

        Map<Integer, Paziente> pazientiUnivoci = new LinkedHashMap<>();
        Map<Integer, LocalDateTime> ultimaVisitaPerPaziente = new LinkedHashMap<>();

        for (Visita v : visite) {
            Paziente p = v.getPrenotazione().getPaziente();
            LocalDateTime dataVisita = v.getPrenotazione().getDataOra();
            pazientiUnivoci.putIfAbsent(p.getId(), p);
            ultimaVisitaPerPaziente.merge(p.getId(), dataVisita,
                    (a, b) -> a.isAfter(b) ? a : b);
        }

        String queryPulita = (query == null) ? "" : query.trim().toLowerCase();

        return pazientiUnivoci.values().stream()
                .filter(p -> queryPulita.isEmpty()
                        || p.getUtente().getNome().toLowerCase().contains(queryPulita)
                        || p.getUtente().getCognome().toLowerCase().contains(queryPulita)
                        || (p.getUtente().getCodiceFiscale() != null
                            && p.getUtente().getCodiceFiscale().toLowerCase().contains(queryPulita)))
                .map(p -> new PazienteInCaricoResponse(
                        p.getId(),
                        p.getUtente().getNome(),
                        p.getUtente().getCognome(),
                        p.getUtente().getCodiceFiscale(),
                        ultimaVisitaPerPaziente.get(p.getId())
                ))
                .sorted(Comparator.comparing(PazienteInCaricoResponse::getUltimaVisita).reversed())
                .toList();
    }

    // RF-13: storico di UN paziente specifico, solo le visite/autorizzazioni di
    // QUESTO medico — mai quelle svolte dal paziente con altri medici (vincolo
    // esplicito del requisito).
    public List<VisitaStoricoItemResponse> getStoricoPaziente(Integer idPaziente) {
        Medico medico = currentUserService.getMedicoCorrente();

        return visitaRepository.findByPrenotazione_Medico_Id(medico.getId()).stream()
                .filter(v -> v.getPrenotazione().getPaziente().getId().equals(idPaziente))
                .sorted(Comparator.comparing((Visita v) -> v.getPrenotazione().getDataOra()).reversed())
                .map(this::toStoricoResponse)
                .toList();
    }

    private VisitaStoricoItemResponse toStoricoResponse(Visita v) {
        Prenotazione p = v.getPrenotazione();
        String nomeServizio = nomeServizioDi(p);
        String nomePrestazioneAutorizzata = v.getPrestazionePreviaVisitaAutorizzata() != null
                ? v.getPrestazionePreviaVisitaAutorizzata().getNome()
                : null;

        return new VisitaStoricoItemResponse(
                p.getId(), p.getDataOra(), nomeServizio, v.getNote(), v.isConclusa(),
                v.isAutorizzatoStepSuccessivo(), nomePrestazioneAutorizzata
        );
    }

    // RF-14: visite passate del medico ancora senza esito registrato.
    public List<VisitaDaGestireItemResponse> getVisiteDaGestire() {
        Medico medico = currentUserService.getMedicoCorrente();
        List<Prenotazione> prenotazioni = prenotazioneRepository
                .findVisiteDaGestireByMedico(medico.getId(), LocalDateTime.now());

        return prenotazioni.stream()
                .map(p -> new VisitaDaGestireItemResponse(
                        p.getId(),
                        p.getPaziente().getId(),
                        p.getDataOra(),
                        p.getPaziente().getUtente().getNome() + " " + p.getPaziente().getUtente().getCognome(),
                        p.getPaziente().getUtente().getCodiceFiscale(),
                        nomeServizioDi(p)
                ))
                .toList();
    }

    // Prossimi appuntamenti (futuri) del medico — sola consultazione, nessuna
    // azione associata. Colma la lacuna per cui il medico non aveva alcuna
    // visibilità su ciò che deve ancora avvenire (solo sul passato, tramite
    // "Visite da gestire").
    public List<VisitaDaGestireItemResponse> getProssimiAppuntamenti() {
        Medico medico = currentUserService.getMedicoCorrente();
        List<Prenotazione> prenotazioni = prenotazioneRepository
                .findProssimiAppuntamentiMedico(medico.getId(), LocalDateTime.now());

        return prenotazioni.stream()
                .map(p -> new VisitaDaGestireItemResponse(
                        p.getId(),
                        p.getPaziente().getId(),
                        p.getDataOra(),
                        p.getPaziente().getUtente().getNome() + " " + p.getPaziente().getUtente().getCognome(),
                        p.getPaziente().getUtente().getCodiceFiscale(),
                        nomeServizioDi(p)
                ))
                .toList();
    }

    // FIX: prima si assumeva sempre prestazioneDiretta — ma una prenotazione
    // assegnata al medico può benissimo essere una prestazione VINCOLATA da lui
    // eseguita (es. "Rimozione nei"), non solo la visita propedeutica. Senza
    // questo controllo, .getPrestazioneDiretta().getNome() va in
    // NullPointerException su quelle righe.
    private String nomeServizioDi(Prenotazione p) {
        return p.getPrestazioneDiretta() != null
                ? p.getPrestazioneDiretta().getNome()
                : p.getPrestazionePreviaVisita().getNome();
    }

    // Prestazioni vincolate autorizzabili a partire da questa visita — funziona
    // sia quando si conclude la VISITA PROPEDEUTICA stessa (caso originale),
    // sia quando si conclude la PRESTAZIONE VINCOLATA già autorizzata (es. il
    // medico, concludendo una "Rimozione nei", vuole autorizzarne subito
    // un'altra per una sessione successiva) — in entrambi i casi si risale
    // allo stesso "tipo di visita propedeutica" di riferimento.
    public List<PrestazioneAutorizzabileResponse> getPrestazioniAutorizzabili(Integer idPrenotazione) {
        Medico medico = currentUserService.getMedicoCorrente();
        Prenotazione prenotazione = prenotazioneRepository.findById(idPrenotazione)
                .orElseThrow(() -> new IllegalArgumentException("Prenotazione non trovata"));

        verificaPrenotazioneDelMedico(prenotazione, medico);

        PrestazioneDiretta visitaPropedeuticaDiRiferimento = visitaPropedeuticaDiRiferimentoDi(prenotazione);
        if (visitaPropedeuticaDiRiferimento == null) {
            return List.of();
        }

        return previaVisitaRepository.findByVisitaPropedeutica_IdAndAttivoTrue(visitaPropedeuticaDiRiferimento.getId())
                .stream()
                .map(pv -> new PrestazioneAutorizzabileResponse(pv.getId(), pv.getNome()))
                .toList();
    }

    // Risale al "tipo di visita propedeutica" di riferimento per questa
    // prenotazione: se è essa stessa una visita propedeutica (prestazioneDiretta
    // valorizzata), è se stessa; se è invece una prestazione vincolata già
    // autorizzata (prestazionePreviaVisita valorizzata), risale al tipo di
    // visita che l'ha originariamente sbloccata.
    private PrestazioneDiretta visitaPropedeuticaDiRiferimentoDi(Prenotazione p) {
        if (p.getPrestazioneDiretta() != null) {
            return p.getPrestazioneDiretta();
        }
        if (p.getPrestazionePreviaVisita() != null) {
            return p.getPrestazionePreviaVisita().getVisitaPropedeutica();
        }
        return null;
    }

    // RF-14: registra l'esito della visita (nota clinica, se conclusa) e,
    // opzionalmente, l'autorizzazione per una specifica prestazione vincolata —
    // "abilita il paziente", che potrà prenotarla lui stesso (o farla prenotare
    // dalla Segreteria) in un secondo momento. Il "prenota lui stesso" di RF-14
    // (il medico prenota subito per conto del paziente) è un'azione separata,
    // successiva: riusa lo stesso meccanismo "prenota per conto" già esistente.
    public void registraEsito(Integer idPrenotazione, EsitoVisitaRequest request) {
        Medico medico = currentUserService.getMedicoCorrente();
        Prenotazione prenotazione = prenotazioneRepository.findById(idPrenotazione)
                .orElseThrow(() -> new IllegalArgumentException("Prenotazione non trovata"));

        verificaPrenotazioneDelMedico(prenotazione, medico);

        if (visitaRepository.existsByPrenotazione_Id(idPrenotazione)) {
            throw new IllegalArgumentException("L'esito per questa visita è già stato registrato");
        }

        Visita visita = new Visita();
        visita.setPrenotazione(prenotazione);
        visita.setNote(request.getNote());
        visita.setConclusa(request.isConclusa());
        // Data della VISITA (l'appuntamento), non dell'istante in cui il medico
        // registra l'esito — potrebbero non coincidere se lo fa con qualche
        // giorno di ritardo rispetto all'incontro reale.
        visita.setDataOra(prenotazione.getDataOra());

        if (request.getIdPrestazionePreviaVisitaAutorizzata() != null) {
            PrestazionePreviaVisita prestazione = previaVisitaRepository
                    .findById(request.getIdPrestazionePreviaVisitaAutorizzata())
                    .orElseThrow(() -> new IllegalArgumentException("Prestazione da autorizzare non trovata"));

            // Difesa: si può autorizzare solo una prestazione il cui "tipo di
            // visita propedeutica" di riferimento è effettivamente quello di
            // QUESTA prenotazione (sia essa la visita propedeutica stessa, sia
            // la prestazione vincolata già autorizzata che si sta concludendo)
            // — altrimenti un medico potrebbe autorizzare qualunque prestazione
            // da qualunque visita, scavalcando il legame reale.
            PrestazioneDiretta visitaPropedeuticaDiRiferimento = visitaPropedeuticaDiRiferimentoDi(prenotazione);
            if (visitaPropedeuticaDiRiferimento == null
                    || !prestazione.getVisitaPropedeutica().getId().equals(visitaPropedeuticaDiRiferimento.getId())) {
                throw new IllegalArgumentException("Questa prestazione non è autorizzabile da questa visita");
            }

            visita.setPrestazionePreviaVisitaAutorizzata(prestazione);
            visita.setAutorizzatoStepSuccessivo(true);
        }

        visitaRepository.save(visita);
    }

    private void verificaPrenotazioneDelMedico(Prenotazione prenotazione, Medico medico) {
        if (prenotazione.getMedico() == null || !prenotazione.getMedico().getId().equals(medico.getId())) {
            throw new IllegalArgumentException("Non sei autorizzato a gestire questa prenotazione");
        }
    }
}