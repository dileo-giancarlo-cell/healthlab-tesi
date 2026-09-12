package com.healthlab.api.service;

import com.healthlab.api.dto.response.PrestazioneResponse;
import com.healthlab.api.dto.response.ServizioResponse;
import com.healthlab.api.repository.PrestazioneDirettaRepository;
import com.healthlab.api.repository.PrestazioneDirettaServiziRepository;
import com.healthlab.api.repository.PrestazionePreviaVisitaRepository;
import com.healthlab.api.repository.SedePrestazioneDirettaRepository;
import com.healthlab.api.repository.SedePrestazionePreviaVisitaRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PrestazioneService {

    private final PrestazioneDirettaRepository direttaRepository;
    private final PrestazionePreviaVisitaRepository previaVisitaRepository;
    private final PrestazioneDirettaServiziRepository prestazioneDirettaServiziRepository;
    private final SedePrestazioneDirettaRepository sedeDirettaRepository;
    private final SedePrestazionePreviaVisitaRepository sedePreviaVisitaRepository;

    public PrestazioneService(PrestazioneDirettaRepository direttaRepository,
                               PrestazionePreviaVisitaRepository previaVisitaRepository,
                               PrestazioneDirettaServiziRepository prestazioneDirettaServiziRepository,
                               SedePrestazioneDirettaRepository sedeDirettaRepository,
                               SedePrestazionePreviaVisitaRepository sedePreviaVisitaRepository) {
        this.direttaRepository = direttaRepository;
        this.previaVisitaRepository = previaVisitaRepository;
        this.prestazioneDirettaServiziRepository = prestazioneDirettaServiziRepository;
        this.sedeDirettaRepository = sedeDirettaRepository;
        this.sedePreviaVisitaRepository = sedePreviaVisitaRepository;
    }

    // RF-17: lato pubblico, solo le prestazioni attive.
    // RF-18: se idSede e' valorizzato, filtra anche per disponibilita' nella
    // sede scelta (in base al collegamento configurato dall'Admin). Se
    // idSede e' null, il comportamento resta invariato (nessun filtro di sede)
    // così il caricamento iniziale di prenotazioni.html, prima che l'utente scelga
    // una sede, continua a funzionare.
    public List<PrestazioneResponse> getByCategoria(Integer idCategoria, Integer idSede) {
        List<PrestazioneResponse> risultato = new ArrayList<>();

        List<com.healthlab.api.entity.PrestazioneDiretta> dirette =
                direttaRepository.findByCategoriaPrestazione_IdAndAttivoTrue(idCategoria);
        if (idSede != null) {
            dirette = dirette.stream()
                    .filter(p -> sedeDirettaRepository.existsBySede_IdAndPrestazioneDiretta_Id(idSede, p.getId()))
                    .toList();
        }
        dirette.forEach(p -> risultato.add(new PrestazioneResponse(p.getId(), p.getNome(), "diretta", true)));

        List<com.healthlab.api.entity.PrestazionePreviaVisita> previaVisita =
                previaVisitaRepository.findByCategoriaPrestazione_IdAndAttivoTrue(idCategoria);
        if (idSede != null) {
            previaVisita = previaVisita.stream()
                    .filter(p -> sedePreviaVisitaRepository.existsBySede_IdAndPrestazionePreviaVisita_Id(idSede, p.getId()))
                    .toList();
        }
        previaVisita.forEach(p -> risultato.add(new PrestazioneResponse(p.getId(), p.getNome(), "previa_visita", false)));

        return risultato;
    }

    public List<ServizioResponse> getServiziByPrestazione(Integer idPrestazioneDiretta) {
        return prestazioneDirettaServiziRepository.findByPrestazioneDiretta_IdAndAttivoTrue(idPrestazioneDiretta).stream()
                .map(s -> new ServizioResponse(s.getId(), s.getNome()))
                .toList();
    }
}