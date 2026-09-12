package com.healthlab.api.entity;

import java.io.Serializable;
import java.util.Objects;

public class SedePrestazionePreviaVisitaId implements Serializable {

    private Integer sede;
    private Integer prestazionePreviaVisita;

    public SedePrestazionePreviaVisitaId() {}

    public SedePrestazionePreviaVisitaId(Integer sede, Integer prestazionePreviaVisita) {
        this.sede = sede;
        this.prestazionePreviaVisita = prestazionePreviaVisita;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SedePrestazionePreviaVisitaId that)) return false;
        return Objects.equals(sede, that.sede) && Objects.equals(prestazionePreviaVisita, that.prestazionePreviaVisita);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sede, prestazionePreviaVisita);
    }
}