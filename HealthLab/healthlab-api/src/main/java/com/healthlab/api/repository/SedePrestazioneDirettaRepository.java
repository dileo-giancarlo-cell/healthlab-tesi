package com.healthlab.api.repository;

import com.healthlab.api.entity.SedePrestazioneDiretta;
import com.healthlab.api.entity.SedePrestazioneDirettaId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SedePrestazioneDirettaRepository extends JpaRepository<SedePrestazioneDiretta, SedePrestazioneDirettaId> {
    List<SedePrestazioneDiretta> findBySede_Id(Integer idSede);
    boolean existsBySede_IdAndPrestazioneDiretta_Id(Integer idSede, Integer idPrestazioneDiretta);
    void deleteBySede_IdAndPrestazioneDiretta_Id(Integer idSede, Integer idPrestazioneDiretta);
}