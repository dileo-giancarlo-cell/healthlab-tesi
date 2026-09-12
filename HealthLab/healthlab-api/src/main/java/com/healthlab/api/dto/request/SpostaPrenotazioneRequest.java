package com.healthlab.api.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SpostaPrenotazioneRequest {
    private LocalDateTime nuovaDataOra;
}