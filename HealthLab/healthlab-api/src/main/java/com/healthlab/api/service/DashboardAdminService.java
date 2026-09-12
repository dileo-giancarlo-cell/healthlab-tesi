package com.healthlab.api.service;

import com.healthlab.api.dto.response.CategoriaConteggioResponse;
import com.healthlab.api.dto.response.DashboardAdminResponse;
import com.healthlab.api.dto.response.GenereConteggioResponse;
import com.healthlab.api.dto.response.MeseConteggioResponse;
import com.healthlab.api.entity.Utente;
import com.healthlab.api.repository.PazienteRepository;
import com.healthlab.api.repository.PrenotazioneRepository;
import com.healthlab.api.security.CurrentUserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardAdminService {

    private final PrenotazioneRepository prenotazioneRepository;
    private final PazienteRepository pazienteRepository;
    private final CurrentUserService currentUserService;

    public DashboardAdminService(PrenotazioneRepository prenotazioneRepository,
                                  PazienteRepository pazienteRepository,
                                  CurrentUserService currentUserService) {
        this.prenotazioneRepository = prenotazioneRepository;
        this.pazienteRepository = pazienteRepository;
        this.currentUserService = currentUserService;
    }

    public DashboardAdminResponse getDashboard(int anno) {
        verificaAdmin();

        LocalDateTime adesso = LocalDateTime.now();

        long totali = prenotazioneRepository.countByAnno(anno);
        long passate = prenotazioneRepository.countByAnnoAndDataOraLessThanEqual(anno, adesso);
        long completate = prenotazioneRepository.countByAnnoAndDataOraLessThanEqualAndAnnullataFalse(anno, adesso);

        // Solo sulle passate
        double tasso = passate > 0 ? (completate * 100.0) / passate : 0.0;

        return new DashboardAdminResponse(
                totali,
                passate,
                completate,
                Math.round(tasso * 10) / 10.0, // un decimale, niente cifre inutili
                getDistribuzionePerCategoria(anno),
                getDistribuzionePerGenere(anno),
                getAndamentoTemporale(anno)
        );
    }

    // Endpoint dedicato per il selettore dell'anno del grafico — il frontend lo
    // richiama da solo quando l'utente scorre tra un anno e l'altro
    public List<MeseConteggioResponse> getAndamentoTemporalePerAnno(int anno) {
        verificaAdmin();
        return getAndamentoTemporale(anno);
    }

    // Le prestazioni dirette e quelle vincolate sono due catene di relazione
    // separate verso Categoria — uniamo i conteggi per nome categoria qui
    // in Java, invece di tentare un JPQL tra due join opzionali
    private List<CategoriaConteggioResponse> getDistribuzionePerCategoria(int anno) {
        Map<String, Long> conteggi = new LinkedHashMap<>();

        for (Object[] riga : prenotazioneRepository.countPerCategoriaDirettaAnno(anno)) {
            String nomeCategoria = (String) riga[0];
            long conteggio = (Long) riga[1];
            conteggi.merge(nomeCategoria, conteggio, Long::sum);
        }
        for (Object[] riga : prenotazioneRepository.countPerCategoriaPreviaVisitaAnno(anno)) {
            String nomeCategoria = (String) riga[0];
            long conteggio = (Long) riga[1];
            conteggi.merge(nomeCategoria, conteggio, Long::sum);
        }

        return conteggi.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue())) // decrescente
                .map(e -> new CategoriaConteggioResponse(e.getKey(), e.getValue()))
                .toList();
    }

    private List<GenereConteggioResponse> getDistribuzionePerGenere(int anno) {
        return prenotazioneRepository.countPazientiDistintiPerSessoEAnno(anno).stream()
                .map(riga -> {
                    // riga[0] è l'enum Paziente.Sesso — .toString() dà "M" o "F"
                    String sesso = riga[0] != null ? riga[0].toString() : "Non specificato";
                    long conteggio = (Long) riga[1];
                    return new GenereConteggioResponse(sesso, conteggio);
                })
                .toList();
    }


    private List<MeseConteggioResponse> getAndamentoTemporale(int anno) {
        Map<String, Long> conteggiPerMese = new LinkedHashMap<>();

        for (Object[] riga : prenotazioneRepository.countPerMese()) {
            int annoRiga = (Integer) riga[0];
            int mese = (Integer) riga[1];
            long conteggio = (Long) riga[2];
            conteggiPerMese.put(annoRiga + "-" + String.format("%02d", mese), conteggio);
        }

        List<MeseConteggioResponse> risultato = new java.util.ArrayList<>();
        for (int mese = 1; mese <= 12; mese++) {
            String meseAnno = anno + "-" + String.format("%02d", mese);
            long conteggio = conteggiPerMese.getOrDefault(meseAnno, 0L);
            risultato.add(new MeseConteggioResponse(meseAnno, conteggio));
        }
        return risultato;
    }

    private void verificaAdmin() {
        Utente utenteCorrente = currentUserService.getUtenteCorrente();
        if (!"amministratore".equals(utenteCorrente.getRuolo().getNome())) {
            throw new IllegalArgumentException("Non sei autorizzato a consultare la dashboard");
        }
    }
}