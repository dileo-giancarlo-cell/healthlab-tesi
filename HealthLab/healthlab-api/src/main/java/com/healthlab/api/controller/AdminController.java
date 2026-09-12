package com.healthlab.api.controller;

import com.healthlab.api.dto.request.CategoriaRequest;
import com.healthlab.api.dto.request.CreaUtenteStaffRequest;
import com.healthlab.api.dto.request.ModificaUtenteStaffRequest;
import com.healthlab.api.dto.request.NomeRequest;
import com.healthlab.api.dto.request.PrestazioneDirettaRequest;
import com.healthlab.api.dto.request.PrestazionePreviaVisitaRequest;
import com.healthlab.api.dto.request.ServizioRequest;
import com.healthlab.api.dto.response.*;
import com.healthlab.api.service.AdminService;
import com.healthlab.api.service.CatalogoAdminService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final CatalogoAdminService catalogoAdminService;

    public AdminController(AdminService adminService, CatalogoAdminService catalogoAdminService) {
        this.adminService = adminService;
        this.catalogoAdminService = catalogoAdminService;
    }

    // ---------- Elenchi per valorizzare i dropdown ----------

    @GetMapping("/sedi")
    public List<SedeResponse> getSedi() {
        return adminService.getSedi();
    }

    @PostMapping("/sedi")
    public SedeResponse creaSede(@RequestBody com.healthlab.api.dto.request.SedeRequest request) {
        return adminService.creaSede(request.getNome(), request.getIndirizzo(), request.getOrario(), request.getTelefono());
    }

    @PutMapping("/sedi/{id}")
    public SedeResponse modificaSede(@PathVariable Integer id, @RequestBody com.healthlab.api.dto.request.SedeRequest request) {
        return adminService.modificaSede(id, request.getNome(), request.getIndirizzo(), request.getOrario(), request.getTelefono());
    }

    @GetMapping("/specializzazioni-medico")
    public List<IdNomeResponse> getSpecializzazioniMedico() {
        return adminService.getSpecializzazioniMedico();
    }

    @GetMapping("/specializzazioni-tecnico")
    public List<IdNomeResponse> getSpecializzazioniTecnico() {
        return adminService.getSpecializzazioniTecnico();
    }

    @PostMapping("/specializzazioni-medico")
    public IdNomeResponse creaSpecializzazioneMedico(@RequestBody NomeRequest request) {
        return adminService.creaSpecializzazioneMedico(request.getNome());
    }

    @PutMapping("/specializzazioni-medico/{id}")
    public IdNomeResponse modificaSpecializzazioneMedico(@PathVariable Integer id, @RequestBody NomeRequest request) {
        return adminService.modificaSpecializzazioneMedico(id, request.getNome());
    }

    @PostMapping("/specializzazioni-tecnico")
    public IdNomeResponse creaSpecializzazioneTecnico(@RequestBody NomeRequest request) {
        return adminService.creaSpecializzazioneTecnico(request.getNome());
    }

    @PutMapping("/specializzazioni-tecnico/{id}")
    public IdNomeResponse modificaSpecializzazioneTecnico(@PathVariable Integer id, @RequestBody NomeRequest request) {
        return adminService.modificaSpecializzazioneTecnico(id, request.getNome());
    }

    // Sola consultazione — vedi commento in AdminService.getRuoli()
    @GetMapping("/ruoli")
    public List<IdNomeResponse> getRuoli() {
        return adminService.getRuoli();
    }

    // ---------- RF-16: gestione utenti ----------

    @GetMapping("/utenti")
    public List<UtenteAdminResponse> getUtentiStaff(
            @RequestParam(required = false) String ruolo,
            @RequestParam(required = false, defaultValue = "") String query) {
        return adminService.getUtentiStaff(ruolo, query);
    }

    @PostMapping("/utenti")
    public UtenteAdminResponse creaUtenteStaff(@RequestBody CreaUtenteStaffRequest request) {
        return adminService.creaUtenteStaff(request);
    }

    @PutMapping("/utenti/{id}")
    public UtenteAdminResponse modificaUtenteStaff(
            @PathVariable Integer id, @RequestBody ModificaUtenteStaffRequest request) {
        return adminService.modificaUtenteStaff(id, request);
    }

    @PatchMapping("/utenti/{id}/disabilita")
    public void disabilitaUtente(@PathVariable Integer id) {
        adminService.disabilitaUtente(id);
    }

    @PatchMapping("/utenti/{id}/riabilita")
    public void riabilitaUtente(@PathVariable Integer id) {
        adminService.riabilitaUtente(id);
    }

    // ---------- RF-17: gestione catalogo servizi ----------

    @GetMapping("/categorie")
    public List<CategoriaAdminResponse> getCategorie() {
        return catalogoAdminService.getCategorie();
    }

    @PostMapping("/categorie")
    public CategoriaAdminResponse creaCategoria(@RequestBody CategoriaRequest request) {
        return catalogoAdminService.creaCategoria(request);
    }

    @PutMapping("/categorie/{id}")
    public CategoriaAdminResponse modificaCategoria(@PathVariable Integer id, @RequestBody CategoriaRequest request) {
        return catalogoAdminService.modificaCategoria(id, request);
    }

    @PatchMapping("/categorie/{id}/sospendi")
    public void sospendiCategoria(@PathVariable Integer id) {
        catalogoAdminService.sospendiCategoria(id);
    }

    @PatchMapping("/categorie/{id}/riattiva")
    public void riattivaCategoria(@PathVariable Integer id) {
        catalogoAdminService.riattivaCategoria(id);
    }

    @PostMapping(value = "/categorie/{id}/immagine", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public CategoriaAdminResponse caricaImmagineCategoria(
            @PathVariable Integer id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        return catalogoAdminService.caricaImmagineCategoria(id, file);
    }

    @GetMapping("/prestazioni-dirette")
    public List<PrestazioneDirettaAdminResponse> getPrestazioniDirette(@RequestParam Integer idCategoria) {
        return catalogoAdminService.getPrestazioniDirette(idCategoria);
    }

    @PostMapping("/prestazioni-dirette")
    public PrestazioneDirettaAdminResponse creaPrestazioneDiretta(@RequestBody PrestazioneDirettaRequest request) {
        return catalogoAdminService.creaPrestazioneDiretta(request);
    }

    @PutMapping("/prestazioni-dirette/{id}")
    public PrestazioneDirettaAdminResponse modificaPrestazioneDiretta(
            @PathVariable Integer id, @RequestBody PrestazioneDirettaRequest request) {
        return catalogoAdminService.modificaPrestazioneDiretta(id, request);
    }

    @PatchMapping("/prestazioni-dirette/{id}/sospendi")
    public void sospendiPrestazioneDiretta(@PathVariable Integer id) {
        catalogoAdminService.sospendiPrestazioneDiretta(id);
    }

    @PatchMapping("/prestazioni-dirette/{id}/riattiva")
    public void riattivaPrestazioneDiretta(@PathVariable Integer id) {
        catalogoAdminService.riattivaPrestazioneDiretta(id);
    }

    @GetMapping("/visite-propedeutiche-disponibili")
    public List<VisitaPropedeuticaOpzioneResponse> getVisitePropedeuticheDisponibili() {
        return catalogoAdminService.getVisitePropedeuticheDisponibili();
    }

    @GetMapping("/prestazioni-previa-visita")
    public List<PrestazionePreviaVisitaAdminResponse> getPrestazioniPreviaVisita(@RequestParam Integer idCategoria) {
        return catalogoAdminService.getPrestazioniPreviaVisita(idCategoria);
    }

    @PostMapping("/prestazioni-previa-visita")
    public PrestazionePreviaVisitaAdminResponse creaPrestazionePreviaVisita(@RequestBody PrestazionePreviaVisitaRequest request) {
        return catalogoAdminService.creaPrestazionePreviaVisita(request);
    }

    @PutMapping("/prestazioni-previa-visita/{id}")
    public PrestazionePreviaVisitaAdminResponse modificaPrestazionePreviaVisita(
            @PathVariable Integer id, @RequestBody PrestazionePreviaVisitaRequest request) {
        return catalogoAdminService.modificaPrestazionePreviaVisita(id, request);
    }

    @PatchMapping("/prestazioni-previa-visita/{id}/sospendi")
    public void sospendiPrestazionePreviaVisita(@PathVariable Integer id) {
        catalogoAdminService.sospendiPrestazionePreviaVisita(id);
    }

    @PatchMapping("/prestazioni-previa-visita/{id}/riattiva")
    public void riattivaPrestazionePreviaVisita(@PathVariable Integer id) {
        catalogoAdminService.riattivaPrestazionePreviaVisita(id);
    }

    @GetMapping("/prestazioni-dirette/{id}/servizi")
    public List<ServizioAdminResponse> getServizi(@PathVariable Integer id) {
        return catalogoAdminService.getServizi(id);
    }

    @PostMapping("/prestazioni-dirette/{id}/servizi")
    public ServizioAdminResponse creaServizio(@PathVariable Integer id, @RequestBody ServizioRequest request) {
        return catalogoAdminService.creaServizio(id, request);
    }

    @PutMapping("/servizi/{id}")
    public ServizioAdminResponse modificaServizio(@PathVariable Integer id, @RequestBody ServizioRequest request) {
        return catalogoAdminService.modificaServizio(id, request);
    }

    @PatchMapping("/servizi/{id}/sospendi")
    public void sospendiServizio(@PathVariable Integer id) {
        catalogoAdminService.sospendiServizio(id);
    }

    @PatchMapping("/servizi/{id}/riattiva")
    public void riattivaServizio(@PathVariable Integer id) {
        catalogoAdminService.riattivaServizio(id);
    }
}