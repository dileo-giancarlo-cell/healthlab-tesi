package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RefertoResponse {
    private Integer id;
    private String nomeServizio;
    private LocalDateTime dataCaricamento;
    private String fileOValore;
}