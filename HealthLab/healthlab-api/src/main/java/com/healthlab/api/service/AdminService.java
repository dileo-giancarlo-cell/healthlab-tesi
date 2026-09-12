package com.healthlab.api.service;

import com.healthlab.api.dto.request.CreaUtenteStaffRequest;
import com.healthlab.api.dto.request.ModificaUtenteStaffRequest;
import com.healthlab.api.dto.response.IdNomeResponse;
import com.healthlab.api.dto.response.SedeResponse;
import com.healthlab.api.dto.response.UtenteAdminResponse;
import com.healthlab.api.entity.*;
import com.healthlab.api.repository.*;
import com.healthlab.api.security.CurrentUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminService {

    private static final List<String> RUOLI_GESTIBILI = List.of("segretario", "tecnico", "medico");

    private final UtenteRepository utenteRepository;
    private final RuoloRepository ruoloRepository;
    private final SedeRepository sedeRepository;
    private final SpecializzazioneMedicoRepository specializzazioneMedicoRepository;
    private final SpecializzazioneTecnicoRepository specializzazioneTecnicoRepository;
    private final SegretarioRepository segretarioRepository;
    private final TecnicoRepository tecnicoRepository;
    private final MedicoRepository medicoRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    public AdminService(UtenteRepository utenteRepository,
                         RuoloRepository ruoloRepository,
                         SedeRepository sedeRepository,
                         SpecializzazioneMedicoRepository specializzazioneMedicoRepository,
                         SpecializzazioneTecnicoRepository specializzazioneTecnicoRepository,
                         SegretarioRepository segretarioRepository,
                         TecnicoRepository tecnicoRepository,
                         MedicoRepository medicoRepository,
                         PasswordEncoder passwordEncoder,
                         CurrentUserService currentUserService) {
        this.utenteRepository = utenteRepository;
        this.ruoloRepository = ruoloRepository;
        this.sedeRepository = sedeRepository;
        this.specializzazioneMedicoRepository = specializzazioneMedicoRepository;
        this.specializzazioneTecnicoRepository = specializzazioneTecnicoRepository;
        this.segretarioRepository = segretarioRepository;
        this.tecnicoRepository = tecnicoRepository;
        this.medicoRepository = medicoRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUserService = currentUserService;
    }

    // ---------- Elenchi di supporto per i menu a tendina del frontend ----------

    public List<SedeResponse> getSedi() {
        verificaAdmin();
        return sedeRepository.findAll().stream()
                .map(s -> new SedeResponse(s.getId(), s.getNome(), s.getIndirizzo(), s.getOrario(), s.getTelefono()))
                .toList();
    }

    public SedeResponse creaSede(String nome, String indirizzo, String orario, String telefono) {
        verificaAdmin();
        com.healthlab.api.entity.Sede sede = new com.healthlab.api.entity.Sede();
        sede.setNome(nome);
        sede.setIndirizzo(indirizzo);
        sede.setOrario(orario);
        sede.setTelefono(telefono);
        com.healthlab.api.entity.Sede salvata = sedeRepository.save(sede);
        return new SedeResponse(salvata.getId(), salvata.getNome(), salvata.getIndirizzo(), salvata.getOrario(), salvata.getTelefono());
    }

    public SedeResponse modificaSede(Integer id, String nome, String indirizzo, String orario, String telefono) {
        verificaAdmin();
        com.healthlab.api.entity.Sede sede = sedeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sede non trovata"));
        sede.setNome(nome);
        sede.setIndirizzo(indirizzo);
        sede.setOrario(orario);
        sede.setTelefono(telefono);
        com.healthlab.api.entity.Sede salvata = sedeRepository.save(sede);
        return new SedeResponse(salvata.getId(), salvata.getNome(), salvata.getIndirizzo(), salvata.getOrario(), salvata.getTelefono());
    }

    public List<IdNomeResponse> getSpecializzazioniMedico() {
        verificaAdmin();
        return specializzazioneMedicoRepository.findAll().stream()
                .map(s -> new IdNomeResponse(s.getId(), s.getNome()))
                .toList();
    }

    public List<IdNomeResponse> getSpecializzazioniTecnico() {
        verificaAdmin();
        return specializzazioneTecnicoRepository.findAll().stream()
                .map(s -> new IdNomeResponse(s.getId(), s.getNome()))
                .toList();
    }

    // Sola consultazione: i ruoli sono verificati direttamente nel codice del
    // backend. Percui, un nuovo ruolo creato non servirebbe a nulla
    // senza modifiche al codice. Nessun crea/modifica esposto di proposito.
    public List<IdNomeResponse> getRuoli() {
        verificaAdmin();
        return ruoloRepository.findAll().stream()
                .map(r -> new IdNomeResponse(r.getId(), r.getNome()))
                .toList();
    }

    public IdNomeResponse creaSpecializzazioneMedico(String nome) {
        verificaAdmin();
        validaNomeSpecializzazioneMedicoUnico(nome, null);
        SpecializzazioneMedico s = new SpecializzazioneMedico();
        s.setNome(nome);
        SpecializzazioneMedico salvata = specializzazioneMedicoRepository.save(s);
        return new IdNomeResponse(salvata.getId(), salvata.getNome());
    }

    public IdNomeResponse modificaSpecializzazioneMedico(Integer id, String nome) {
        verificaAdmin();
        validaNomeSpecializzazioneMedicoUnico(nome, id);
        SpecializzazioneMedico s = specializzazioneMedicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Specializzazione non trovata"));
        s.setNome(nome);
        SpecializzazioneMedico salvata = specializzazioneMedicoRepository.save(s);
        return new IdNomeResponse(salvata.getId(), salvata.getNome());
    }

    public IdNomeResponse creaSpecializzazioneTecnico(String nome) {
        verificaAdmin();
        validaNomeSpecializzazioneTecnicoUnico(nome, null);
        SpecializzazioneTecnico s = new SpecializzazioneTecnico();
        s.setNome(nome);
        SpecializzazioneTecnico salvata = specializzazioneTecnicoRepository.save(s);
        return new IdNomeResponse(salvata.getId(), salvata.getNome());
    }

    public IdNomeResponse modificaSpecializzazioneTecnico(Integer id, String nome) {
        verificaAdmin();
        validaNomeSpecializzazioneTecnicoUnico(nome, id);
        SpecializzazioneTecnico s = specializzazioneTecnicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Specializzazione non trovata"));
        s.setNome(nome);
        SpecializzazioneTecnico salvata = specializzazioneTecnicoRepository.save(s);
        return new IdNomeResponse(salvata.getId(), salvata.getNome());
    }


    private void validaNomeSpecializzazioneMedicoUnico(String nome, Integer idEscluso) {
        boolean duplicato = specializzazioneMedicoRepository.findAll().stream()
                .anyMatch(s -> s.getNome().equalsIgnoreCase(nome) && (idEscluso == null || !s.getId().equals(idEscluso)));
        if (duplicato) {
            throw new IllegalArgumentException("Una specializzazione medica con questo nome esiste già.");
        }
    }

    private void validaNomeSpecializzazioneTecnicoUnico(String nome, Integer idEscluso) {
        boolean duplicato = specializzazioneTecnicoRepository.findAll().stream()
                .anyMatch(s -> s.getNome().equalsIgnoreCase(nome) && (idEscluso == null || !s.getId().equals(idEscluso)));
        if (duplicato) {
            throw new IllegalArgumentException("Una specializzazione tecnica con questo nome esiste già.");
        }
    }

    // ---------- RF-16: gestione utenti ----------

    public List<UtenteAdminResponse> getUtentiStaff(String ruoloFiltro, String query) {
        verificaAdmin();

        List<String> ruoliDaCercare = (ruoloFiltro != null && !ruoloFiltro.isBlank())
                ? List.of(ruoloFiltro)
                : RUOLI_GESTIBILI;

        String queryPulita = (query == null) ? "" : query.trim().toLowerCase();

        return utenteRepository.findByRuolo_NomeIn(ruoliDaCercare).stream()
                .filter(u -> queryPulita.isEmpty()
                        || u.getNome().toLowerCase().contains(queryPulita)
                        || u.getCognome().toLowerCase().contains(queryPulita)
                        || u.getUsername().toLowerCase().contains(queryPulita)
                        || u.getEmail().toLowerCase().contains(queryPulita))
                .map(this::toAdminResponse)
                .toList();
    }

    public UtenteAdminResponse creaUtenteStaff(CreaUtenteStaffRequest request) {
        verificaAdmin();

        if (!RUOLI_GESTIBILI.contains(request.getRuolo())) {
            throw new IllegalArgumentException("Ruolo non gestibile da questa funzione");
        }
        if (utenteRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Nome utente già in uso.");
        }
        if (utenteRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email già registrata.");
        }

        Ruolo ruolo = ruoloRepository.findByNome(request.getRuolo())
                .orElseThrow(() -> new IllegalStateException("Ruolo '" + request.getRuolo() + "' non configurato"));

        Utente utente = new Utente();
        utente.setNome(request.getNome());
        utente.setCognome(request.getCognome());
        utente.setUsername(request.getUsername());
        utente.setEmail(request.getEmail());
        utente.setTelefono(request.getTelefono());
        utente.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        utente.setRuolo(ruolo);
        utente.setDataCreazione(LocalDateTime.now());
        utente.setDisabilitato(false);
        // Lo staff creato dall'Admin non passa dal flusso di verifica email
        // (stesso principio già applicato nella registrazione di un nuovo cliente
        // da parte della Segreteria
        utente.setEmailVerificata(true);

        Utente utenteSalvato = utenteRepository.save(utente);

        creaRigaRuoloSpecifica(utenteSalvato, request);

        return toAdminResponse(utenteSalvato);
    }

    private void creaRigaRuoloSpecifica(Utente utente, CreaUtenteStaffRequest request) {
        switch (request.getRuolo()) {
            case "segretario" -> {
                Segretario segretario = new Segretario();
                segretario.setUtente(utente);
                segretario.setSede(trovaSede(request.getIdSede()));
                segretarioRepository.save(segretario);
            }
            case "tecnico" -> {
                Tecnico tecnico = new Tecnico();
                tecnico.setUtente(utente);
                tecnico.setSede(trovaSede(request.getIdSede()));
                tecnico.setSpecializzazione(trovaSpecializzazioneTecnico(request.getIdSpecializzazione()));
                tecnicoRepository.save(tecnico);
            }
            case "medico" -> {
                Medico medico = new Medico();
                medico.setUtente(utente);
                medico.setSede(trovaSede(request.getIdSede()));
                medico.setSpecializzazione(trovaSpecializzazioneMedico(request.getIdSpecializzazione()));
                medico.setNumeroAlbo(request.getNumeroAlbo());
                medico.setBiografia(request.getBiografia());
                medicoRepository.save(medico);
            }
        }
    }

    public UtenteAdminResponse modificaUtenteStaff(Integer idUtente, ModificaUtenteStaffRequest request) {
        verificaAdmin();

        Utente utente = utenteRepository.findById(idUtente)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));

        String ruoloNome = utente.getRuolo().getNome();
        if (!RUOLI_GESTIBILI.contains(ruoloNome)) {
            throw new IllegalArgumentException("Questo utente non è gestibile da questa funzione");
        }

        if (request.getNome() != null) utente.setNome(request.getNome());
        if (request.getCognome() != null) utente.setCognome(request.getCognome());
        if (request.getEmail() != null) utente.setEmail(request.getEmail());
        if (request.getTelefono() != null) utente.setTelefono(request.getTelefono());
        if (request.getNuovaPassword() != null && !request.getNuovaPassword().isBlank()) {
            utente.setPasswordHash(passwordEncoder.encode(request.getNuovaPassword()));
        }
        utenteRepository.save(utente);

        aggiornaRigaRuoloSpecifica(idUtente, ruoloNome, request);

        return toAdminResponse(utente);
    }

    private void aggiornaRigaRuoloSpecifica(Integer idUtente, String ruoloNome, ModificaUtenteStaffRequest request) {
        switch (ruoloNome) {
            case "segretario" -> {
                Segretario segretario = segretarioRepository.findById(idUtente)
                        .orElseThrow(() -> new IllegalStateException("Riga Segretario mancante per l'utente " + idUtente));
                if (request.getIdSede() != null) segretario.setSede(trovaSede(request.getIdSede()));
                segretarioRepository.save(segretario);
            }
            case "tecnico" -> {
                Tecnico tecnico = tecnicoRepository.findById(idUtente)
                        .orElseThrow(() -> new IllegalStateException("Riga Tecnico mancante per l'utente " + idUtente));
                if (request.getIdSede() != null) tecnico.setSede(trovaSede(request.getIdSede()));
                if (request.getIdSpecializzazione() != null) tecnico.setSpecializzazione(trovaSpecializzazioneTecnico(request.getIdSpecializzazione()));
                tecnicoRepository.save(tecnico);
            }
            case "medico" -> {
                Medico medico = medicoRepository.findById(idUtente)
                        .orElseThrow(() -> new IllegalStateException("Riga Medico mancante per l'utente " + idUtente));
                if (request.getIdSede() != null) medico.setSede(trovaSede(request.getIdSede()));
                if (request.getIdSpecializzazione() != null) medico.setSpecializzazione(trovaSpecializzazioneMedico(request.getIdSpecializzazione()));
                if (request.getNumeroAlbo() != null) medico.setNumeroAlbo(request.getNumeroAlbo());
                if (request.getBiografia() != null) medico.setBiografia(request.getBiografia());
                medicoRepository.save(medico);
            }
        }
    }

    public void disabilitaUtente(Integer idUtente) {
        verificaAdmin();
        Utente utente = trovaUtenteGestibile(idUtente);
        utente.setDisabilitato(true);
        utenteRepository.save(utente);
    }

    public void riabilitaUtente(Integer idUtente) {
        verificaAdmin();
        Utente utente = trovaUtenteGestibile(idUtente);
        utente.setDisabilitato(false);
        utenteRepository.save(utente);
    }

    private Utente trovaUtenteGestibile(Integer idUtente) {
        Utente utente = utenteRepository.findById(idUtente)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));
        if (!RUOLI_GESTIBILI.contains(utente.getRuolo().getNome())) {
            throw new IllegalArgumentException("Questo utente non è gestibile da questa funzione");
        }
        return utente;
    }

    // ---------- Helper privati ----------

    private void verificaAdmin() {
        Utente utenteCorrente = currentUserService.getUtenteCorrente();
        if (!"amministratore".equals(utenteCorrente.getRuolo().getNome())) {
            throw new IllegalArgumentException("Non sei autorizzato a eseguire questa operazione");
        }
    }

    private Sede trovaSede(Integer idSede) {
        if (idSede == null) {
            throw new IllegalArgumentException("Sede obbligatoria per questo ruolo");
        }
        return sedeRepository.findById(idSede)
                .orElseThrow(() -> new IllegalArgumentException("Sede non trovata"));
    }

    private SpecializzazioneMedico trovaSpecializzazioneMedico(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Specializzazione obbligatoria per il Medico");
        }
        return specializzazioneMedicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Specializzazione non trovata"));
    }

    private SpecializzazioneTecnico trovaSpecializzazioneTecnico(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Specializzazione obbligatoria per il Tecnico");
        }
        return specializzazioneTecnicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Specializzazione non trovata"));
    }

    private UtenteAdminResponse toAdminResponse(Utente utente) {
        String ruoloNome = utente.getRuolo().getNome();
        String nomeSede = null;
        String nomeSpecializzazione = null;
        String numeroAlbo = null;

        if ("segretario".equals(ruoloNome)) {
            var s = segretarioRepository.findById(utente.getId()).orElse(null);
            if (s != null) {
                nomeSede = s.getSede().getNome();
            }
        } else if ("tecnico".equals(ruoloNome)) {
            var t = tecnicoRepository.findById(utente.getId()).orElse(null);
            if (t != null) {
                nomeSede = t.getSede().getNome();
                nomeSpecializzazione = t.getSpecializzazione().getNome();
            }
        } else if ("medico".equals(ruoloNome)) {
            var m = medicoRepository.findById(utente.getId()).orElse(null);
            if (m != null) {
                nomeSede = m.getSede().getNome();
                nomeSpecializzazione = m.getSpecializzazione().getNome();
                numeroAlbo = m.getNumeroAlbo();
            }
        }

        return new UtenteAdminResponse(
                utente.getId(),
                utente.getNome(),
                utente.getCognome(),
                utente.getUsername(),
                utente.getEmail(),
                utente.getTelefono(),
                ruoloNome,
                utente.isDisabilitato(),
                utente.isEmailVerificata(),
                nomeSede,
                nomeSpecializzazione,
                numeroAlbo
        );
    }
}