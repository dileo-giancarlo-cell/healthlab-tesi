package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DashboardAdminResponse {
    private long prenotazioniTotali;
    private long prenotazioniPassate;
    private long prenotazioniCompletate;
    // Percentuale (0-100), calcolata solo sulle prenotazioni passate: quelle
    // future non hanno ancora avuto modo di "completarsi" o essere annullate.
    private double tassoCompletamento;
    private List<CategoriaConteggioResponse> distribuzionePerCategoria;
    private List<GenereConteggioResponse> distribuzionePerGenere;
    private List<MeseConteggioResponse> andamentoTemporale;
}