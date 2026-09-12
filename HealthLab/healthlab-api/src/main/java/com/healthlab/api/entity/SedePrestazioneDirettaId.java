package com.healthlab.api.entity;

import java.io.Serializable;
import java.util.Objects;

public class SedePrestazioneDirettaId implements Serializable {

    private Integer sede;
    private Integer prestazioneDiretta;

    public SedePrestazioneDirettaId() {}

    public SedePrestazioneDirettaId(Integer sede, Integer prestazioneDiretta) {
        this.sede = sede;
        this.prestazioneDiretta = prestazioneDiretta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SedePrestazioneDirettaId that)) return false;
        return Objects.equals(sede, that.sede) && Objects.equals(prestazioneDiretta, that.prestazioneDiretta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sede, prestazioneDiretta);
    }
}