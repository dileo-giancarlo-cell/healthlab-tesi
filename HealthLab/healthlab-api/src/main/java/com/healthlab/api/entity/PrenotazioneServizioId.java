package com.healthlab.api.entity;

import java.io.Serializable;
import java.util.Objects;

public class PrenotazioneServizioId implements Serializable {

    private Integer prenotazione;
    private Integer servizio;

    public PrenotazioneServizioId() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PrenotazioneServizioId that)) return false;
        return Objects.equals(prenotazione, that.prenotazione) && Objects.equals(servizio, that.servizio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(prenotazione, servizio);
    }
}