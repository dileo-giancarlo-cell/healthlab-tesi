package com.healthlab.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PrenotazioneRequest {

    // Uno dei due va valorizzato (controllo fatto nel Service)
    private Integer idPrestazioneDiretta;
    private Integer idPrestazionePreviaVisita;

    // Valorizzati solo se pertinenti al tipo di servizio scelto
    private Integer idMedico;
    private Integer idTecnico;

    // Valorizzato SOLO quando è il Segretario a prenotare per conto di un
    // paziente (RF-10bis). Se null, la prenotazione riguarda l'utente
    // autenticato stesso (comportamento normale per il Paziente).
    // Il Service verifica che solo un segretario possa valorizzarlo.
    private Integer idPaziente;

    // Obbligatorio SOLO per il Paziente (scelto nel dropdown di
    // prenotazioni.html): per Segretario/Medico viene sempre derivata
    // automaticamente dalla loro sede di appartenenza, questo campo viene
    // ignorato anche se presente. Vedi PrenotazioneService.risolviSedeEffettiva.
    private Integer idSede;

    private List<Integer> idServiziSelezionati;

    @NotNull
    private LocalDateTime dataOra;
}