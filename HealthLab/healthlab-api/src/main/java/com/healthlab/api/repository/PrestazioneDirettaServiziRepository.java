package com.healthlab.api.repository;

import com.healthlab.api.entity.PrestazioneDirettaServizi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrestazioneDirettaServiziRepository extends JpaRepository<PrestazioneDirettaServizi, Integer> {
    // Admin: vede tutto, compresi i sospesi
    List<PrestazioneDirettaServizi> findByPrestazioneDiretta_Id(Integer idPrestazioneDiretta);

    // Lato pubblico: solo gli attivi (checkbox mostrate al momento di prenotare)
    List<PrestazioneDirettaServizi> findByPrestazioneDiretta_IdAndAttivoTrue(Integer idPrestazioneDiretta);
}