package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GenereConteggioResponse {
    private String sesso; // "M" | "F"
    private long conteggio;
}