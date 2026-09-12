package com.healthlab.api.repository;

import com.healthlab.api.entity.SpecializzazioneMedico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecializzazioneMedicoRepository extends JpaRepository<SpecializzazioneMedico, Integer> {
}