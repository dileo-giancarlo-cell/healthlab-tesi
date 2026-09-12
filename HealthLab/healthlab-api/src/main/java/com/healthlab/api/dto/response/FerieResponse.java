package com.healthlab.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class FerieResponse {
    private Integer id;
    private LocalDate dataInizio;
    private LocalDate dataFine;
}