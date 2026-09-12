package com.healthlab.api.service;

import com.healthlab.api.dto.response.SedeResponse;
import com.healthlab.api.repository.SedeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SedeService {

    private final SedeRepository sedeRepository;

    public SedeService(SedeRepository sedeRepository) {
        this.sedeRepository = sedeRepository;
    }

    public List<SedeResponse> getTutte() {
        return sedeRepository.findAll().stream()
                .map(s -> new SedeResponse(s.getId(), s.getNome(), s.getIndirizzo(), s.getOrario(), s.getTelefono()))
                .toList();
    }
}