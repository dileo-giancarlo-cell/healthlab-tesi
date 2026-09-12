package com.healthlab.api.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class InserisciFerieRequest {
    private LocalDate dataInizio;
    private LocalDate dataFine;
}