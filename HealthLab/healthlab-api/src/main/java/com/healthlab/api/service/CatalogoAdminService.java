package com.healthlab.api.service;

import com.healthlab.api.dto.request.CategoriaRequest;
import com.healthlab.api.dto.request.PrestazioneDirettaRequest;
import com.healthlab.api.dto.request.PrestazionePreviaVisitaRequest;
import com.healthlab.api.dto.request.ServizioRequest;
import com.healthlab.api.dto.response.*;
import com.healthlab.api.entity.*;
import com.healthlab.api.repository.*;
import com.healthlab.api.security.CurrentUserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogoAdminService {

    private final CategoriaPrestazioneRepository categoriaRepository;
    private final PrestazioneDirettaRepository direttaRepository;
    private final PrestazionePreviaVisitaRepository previaVisitaRepository;
    private final PrestazioneDirettaServiziRepository servizioRepository;
    private final SpecializzazioneMedicoRepository specializzazioneMedicoRepository;
    private final SpecializzazioneTecnicoRepository specializzazioneTecnicoRepository;
    private final FileStorageService fileStorageService;
    private final CurrentUserService currentUserService;

    public CatalogoAdminService(CategoriaPrestazioneRepository categoriaRepository,
                                 PrestazioneDirettaRepository direttaRepository,
                                 PrestazionePreviaVisitaRepository previaVisitaRepository,
                                 PrestazioneDirettaServiziRepository servizioRepository,
                                 SpecializzazioneMedicoRepository specializzazioneMedicoRepository,
                                 SpecializzazioneTecnicoRepository specializzazioneTecnicoRepository,
                                 FileStorageService fileStorageService,
                                 CurrentUserService currentUserService) {
        this.categoriaRepository = categoriaRepository;
        this.direttaRepository = direttaRepository;
        this.previaVisitaRepository = previaVisitaRepository;
        this.servizioRepository = servizioRepository;
        this.specializzazioneMedicoRepository = specializzazioneMedicoRepository;
        this.specializzazioneTecnicoRepository = specializzazioneTecnicoRepository;
        this.fileStorageService = fileStorageService;
        this.currentUserService = currentUserService;
    }

    // ---------- Categorie ----------

    public List<CategoriaAdminResponse> getCategorie() {
        verificaAdmin();
        return categoriaRepository.findAll().stream()
                .map(c -> new CategoriaAdminResponse(c.getId(), c.getNome(), c.getSlug(), c.isAttivo(), c.getUrlImmagine()))
                .toList();
    }

    public CategoriaAdminResponse creaCategoria(CategoriaRequest request) {
        verificaAdmin();
        validaSlugUnico(request.getSlug(), null);

        CategoriaPrestazione categoria = new CategoriaPrestazione();
        categoria.setNome(request.getNome());
        categoria.setSlug(request.getSlug());
        // urlImmagine resta null alla creazione: si carica dopo, con
        // caricaImmagineCategoria(). Utilizzo lo slug della categoria già
        // esistente per costruire il nome del file standardizzato.
        categoria.setAttivo(true);

        CategoriaPrestazione salvata = categoriaRepository.save(categoria);
        return new CategoriaAdminResponse(salvata.getId(), salvata.getNome(), salvata.getSlug(), salvata.isAttivo(), salvata.getUrlImmagine());
    }

    public CategoriaAdminResponse modificaCategoria(Integer id, CategoriaRequest request) {
        verificaAdmin();
        CategoriaPrestazione categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria non trovata"));

        validaSlugUnico(request.getSlug(), id);

        categoria.setNome(request.getNome());
        categoria.setSlug(request.getSlug());

        CategoriaPrestazione salvata = categoriaRepository.save(categoria);
        return new CategoriaAdminResponse(salvata.getId(), salvata.getNome(), salvata.getSlug(), salvata.isAttivo(), salvata.getUrlImmagine());
    }

    // Nome file basato sullo SLUG (e non sul nome visualizzato):
    // univoco, senza spazi o caratteri speciali
    public CategoriaAdminResponse caricaImmagineCategoria(Integer id, org.springframework.web.multipart.MultipartFile file) {
        verificaAdmin();
        CategoriaPrestazione categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria non trovata"));

        String nomeBase = "categoria-" + categoria.getSlug();
        String nomeFile = fileStorageService.salvaImmagineCategoria(file, nomeBase);

        categoria.setUrlImmagine("/categorie/immagine/" + nomeFile);
        CategoriaPrestazione salvata = categoriaRepository.save(categoria);

        return new CategoriaAdminResponse(salvata.getId(), salvata.getNome(), salvata.getSlug(), salvata.isAttivo(), salvata.getUrlImmagine());
    }

    public void sospendiCategoria(Integer id) {
        verificaAdmin();
        CategoriaPrestazione categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria non trovata"));
        categoria.setAttivo(false);
        categoriaRepository.save(categoria);
    }

    public void riattivaCategoria(Integer id) {
        verificaAdmin();
        CategoriaPrestazione categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria non trovata"));
        categoria.setAttivo(true);
        categoriaRepository.save(categoria);
    }

    private void validaSlugUnico(String slug, Integer idEscluso) {
        categoriaRepository.findBySlug(slug).ifPresent(esistente -> {
            if (idEscluso == null || !esistente.getId().equals(idEscluso)) {
                throw new IllegalArgumentException("Uno slug identico esiste già: " + slug);
            }
        });
    }

    // ---------- Prestazioni dirette ----------

    public List<PrestazioneDirettaAdminResponse> getPrestazioniDirette(Integer idCategoria) {
        verificaAdmin();
        return direttaRepository.findByCategoriaPrestazione_Id(idCategoria).stream()
                .map(this::toDirettaResponse)
                .toList();
    }

    public PrestazioneDirettaAdminResponse creaPrestazioneDiretta(PrestazioneDirettaRequest request) {
        verificaAdmin();
        validaEsecutoreUnico(request.getIdSpecializzazioneMedico(), request.getIdSpecializzazioneTecnico());

        PrestazioneDiretta prestazione = new PrestazioneDiretta();
        prestazione.setNome(request.getNome());
        prestazione.setCategoriaPrestazione(risolviCategoria(request.getIdCategoria()));
        prestazione.setSpecializzazioneMedico(risolviSpecMedicoONull(request.getIdSpecializzazioneMedico()));
        prestazione.setSpecializzazioneTecnico(risolviSpecTecnicoONull(request.getIdSpecializzazioneTecnico()));
        prestazione.setAttivo(true);

        return toDirettaResponse(direttaRepository.save(prestazione));
    }

    public PrestazioneDirettaAdminResponse modificaPrestazioneDiretta(Integer id, PrestazioneDirettaRequest request) {
        verificaAdmin();
        validaEsecutoreUnico(request.getIdSpecializzazioneMedico(), request.getIdSpecializzazioneTecnico());

        PrestazioneDiretta prestazione = direttaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prestazione non trovata"));

        prestazione.setNome(request.getNome());
        prestazione.setCategoriaPrestazione(risolviCategoria(request.getIdCategoria()));
        prestazione.setSpecializzazioneMedico(risolviSpecMedicoONull(request.getIdSpecializzazioneMedico()));
        prestazione.setSpecializzazioneTecnico(risolviSpecTecnicoONull(request.getIdSpecializzazioneTecnico()));

        return toDirettaResponse(direttaRepository.save(prestazione));
    }

    public void sospendiPrestazioneDiretta(Integer id) {
        verificaAdmin();
        PrestazioneDiretta prestazione = direttaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prestazione non trovata"));
        prestazione.setAttivo(false);
        direttaRepository.save(prestazione);
    }

    public void riattivaPrestazioneDiretta(Integer id) {
        verificaAdmin();
        PrestazioneDiretta prestazione = direttaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prestazione non trovata"));
        prestazione.setAttivo(true);
        direttaRepository.save(prestazione);
    }

    private PrestazioneDirettaAdminResponse toDirettaResponse(PrestazioneDiretta p) {
        return new PrestazioneDirettaAdminResponse(
                p.getId(), p.getNome(), p.isAttivo(),
                p.getCategoriaPrestazione().getId(), p.getCategoriaPrestazione().getNome(),
                p.getSpecializzazioneMedico() != null ? p.getSpecializzazioneMedico().getId() : null,
                p.getSpecializzazioneMedico() != null ? p.getSpecializzazioneMedico().getNome() : null,
                p.getSpecializzazioneTecnico() != null ? p.getSpecializzazioneTecnico().getId() : null,
                p.getSpecializzazioneTecnico() != null ? p.getSpecializzazioneTecnico().getNome() : null
        );
    }

    // Per il menu "visita propedeutica" quando si crea/modifica una prestazione
    // vincolata: solo prestazioni dirette eseguite da un Medico e attive.
    public List<VisitaPropedeuticaOpzioneResponse> getVisitePropedeuticheDisponibili() {
        verificaAdmin();
        return direttaRepository.findBySpecializzazioneMedicoIsNotNullAndAttivoTrue().stream()
                .map(p -> new VisitaPropedeuticaOpzioneResponse(p.getId(), p.getNome(), p.getCategoriaPrestazione().getNome()))
                .toList();
    }

    // ---------- Prestazioni vincolate ----------

    public List<PrestazionePreviaVisitaAdminResponse> getPrestazioniPreviaVisita(Integer idCategoria) {
        verificaAdmin();
        return previaVisitaRepository.findByCategoriaPrestazione_Id(idCategoria).stream()
                .map(this::toPreviaVisitaResponse)
                .toList();
    }

    public PrestazionePreviaVisitaAdminResponse creaPrestazionePreviaVisita(PrestazionePreviaVisitaRequest request) {
        verificaAdmin();
        validaEsecutoreUnico(request.getIdSpecializzazioneMedico(), request.getIdSpecializzazioneTecnico());

        PrestazionePreviaVisita prestazione = new PrestazionePreviaVisita();
        prestazione.setNome(request.getNome());
        prestazione.setCategoriaPrestazione(risolviCategoria(request.getIdCategoria()));
        prestazione.setVisitaPropedeutica(risolviVisitaPropedeutica(request.getIdVisitaPropedeutica()));
        prestazione.setSpecializzazioneMedico(risolviSpecMedicoONull(request.getIdSpecializzazioneMedico()));
        prestazione.setSpecializzazioneTecnico(risolviSpecTecnicoONull(request.getIdSpecializzazioneTecnico()));
        prestazione.setAttivo(true);

        return toPreviaVisitaResponse(previaVisitaRepository.save(prestazione));
    }

    public PrestazionePreviaVisitaAdminResponse modificaPrestazionePreviaVisita(Integer id, PrestazionePreviaVisitaRequest request) {
        verificaAdmin();
        validaEsecutoreUnico(request.getIdSpecializzazioneMedico(), request.getIdSpecializzazioneTecnico());

        PrestazionePreviaVisita prestazione = previaVisitaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prestazione non trovata"));

        prestazione.setNome(request.getNome());
        prestazione.setCategoriaPrestazione(risolviCategoria(request.getIdCategoria()));
        prestazione.setVisitaPropedeutica(risolviVisitaPropedeutica(request.getIdVisitaPropedeutica()));
        prestazione.setSpecializzazioneMedico(risolviSpecMedicoONull(request.getIdSpecializzazioneMedico()));
        prestazione.setSpecializzazioneTecnico(risolviSpecTecnicoONull(request.getIdSpecializzazioneTecnico()));

        return toPreviaVisitaResponse(previaVisitaRepository.save(prestazione));
    }

    public void sospendiPrestazionePreviaVisita(Integer id) {
        verificaAdmin();
        PrestazionePreviaVisita prestazione = previaVisitaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prestazione non trovata"));
        prestazione.setAttivo(false);
        previaVisitaRepository.save(prestazione);
    }

    public void riattivaPrestazionePreviaVisita(Integer id) {
        verificaAdmin();
        PrestazionePreviaVisita prestazione = previaVisitaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Prestazione non trovata"));
        prestazione.setAttivo(true);
        previaVisitaRepository.save(prestazione);
    }

    private PrestazionePreviaVisitaAdminResponse toPreviaVisitaResponse(PrestazionePreviaVisita p) {
        return new PrestazionePreviaVisitaAdminResponse(
                p.getId(), p.getNome(), p.isAttivo(),
                p.getCategoriaPrestazione().getId(), p.getCategoriaPrestazione().getNome(),
                p.getVisitaPropedeutica().getId(), p.getVisitaPropedeutica().getNome(),
                p.getSpecializzazioneMedico() != null ? p.getSpecializzazioneMedico().getId() : null,
                p.getSpecializzazioneMedico() != null ? p.getSpecializzazioneMedico().getNome() : null,
                p.getSpecializzazioneTecnico() != null ? p.getSpecializzazioneTecnico().getId() : null,
                p.getSpecializzazioneTecnico() != null ? p.getSpecializzazioneTecnico().getNome() : null
        );
    }

    // ---------- Sotto-parametri (Prestazione_Diretta_Servizi) ----------

    public List<ServizioAdminResponse> getServizi(Integer idPrestazioneDiretta) {
        verificaAdmin();
        return servizioRepository.findByPrestazioneDiretta_Id(idPrestazioneDiretta).stream()
                .map(s -> new ServizioAdminResponse(s.getId(), s.getNome(), s.isAttivo()))
                .toList();
    }

    public ServizioAdminResponse creaServizio(Integer idPrestazioneDiretta, ServizioRequest request) {
        verificaAdmin();
        PrestazioneDiretta prestazione = direttaRepository.findById(idPrestazioneDiretta)
                .orElseThrow(() -> new IllegalArgumentException("Prestazione non trovata"));

        PrestazioneDirettaServizi servizio = new PrestazioneDirettaServizi();
        servizio.setPrestazioneDiretta(prestazione);
        servizio.setNome(request.getNome());
        servizio.setAttivo(true);

        PrestazioneDirettaServizi salvato = servizioRepository.save(servizio);
        return new ServizioAdminResponse(salvato.getId(), salvato.getNome(), salvato.isAttivo());
    }

    public ServizioAdminResponse modificaServizio(Integer id, ServizioRequest request) {
        verificaAdmin();
        PrestazioneDirettaServizi servizio = servizioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Parametro non trovato"));
        servizio.setNome(request.getNome());
        PrestazioneDirettaServizi salvato = servizioRepository.save(servizio);
        return new ServizioAdminResponse(salvato.getId(), salvato.getNome(), salvato.isAttivo());
    }

    public void sospendiServizio(Integer id) {
        verificaAdmin();
        PrestazioneDirettaServizi servizio = servizioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Parametro non trovato"));
        servizio.setAttivo(false);
        servizioRepository.save(servizio);
    }

    public void riattivaServizio(Integer id) {
        verificaAdmin();
        PrestazioneDirettaServizi servizio = servizioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Parametro non trovato"));
        servizio.setAttivo(true);
        servizioRepository.save(servizio);
    }

    // ---------- Helper privati ----------

    private void verificaAdmin() {
        Utente utenteCorrente = currentUserService.getUtenteCorrente();
        if (!"amministratore".equals(utenteCorrente.getRuolo().getNome())) {
            throw new IllegalArgumentException("Non sei autorizzato a eseguire questa operazione");
        }
    }

    // Solo uno dei due esecutori dev'essere valorizzato — stesso vincolo
    // del CHECK a livello di database, anticipato qui per un discorso di errore leggibile.
    private void validaEsecutoreUnico(Integer idSpecMedico, Integer idSpecTecnico) {
        boolean medicoValorizzato = idSpecMedico != null;
        boolean tecnicoValorizzato = idSpecTecnico != null;
        if (medicoValorizzato == tecnicoValorizzato) {
            throw new IllegalArgumentException(
                    "Scegli solo un esecutore: medico oppure tecnico. Né entrambi, né nessuno.");
        }
    }

    private CategoriaPrestazione risolviCategoria(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Categoria obbligatoria");
        }
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria non trovata"));
    }

    private PrestazioneDiretta risolviVisitaPropedeutica(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("Visita propedeutica obbligatoria per una prestazione vincolata");
        }
        PrestazioneDiretta visita = direttaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Visita propedeutica non trovata"));

        // Una visita propedeutica dev'essere per forza eseguita da un
        // Medico (è una valutazione clinica) — non ha senso sbloccare qualcosa
        // a partire da una prestazione eseguita da un Tecnico.
        if (visita.getSpecializzazioneMedico() == null) {
            throw new IllegalArgumentException(
                    "La visita propedeutica scelta non è eseguita da un Medico: non può fare da visita propedeutica.");
        }
        return visita;
    }

    private SpecializzazioneMedico risolviSpecMedicoONull(Integer id) {
        if (id == null) return null;
        return specializzazioneMedicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Specializzazione medico non trovata"));
    }

    private SpecializzazioneTecnico risolviSpecTecnicoONull(Integer id) {
        if (id == null) return null;
        return specializzazioneTecnicoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Specializzazione tecnico non trovata"));
    }
}