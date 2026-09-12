package com.healthlab.api.repository;

import com.healthlab.api.entity.PrenotazioneServizio;
import com.healthlab.api.entity.PrenotazioneServizioId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrenotazioneServizioRepository extends JpaRepository<PrenotazioneServizio, PrenotazioneServizioId> {
}