package com.healthlab.api.controller;

import com.healthlab.api.dto.response.ServizioSedeResponse;
import com.healthlab.api.service.SedeAdminService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sedi")
public class SedeAdminController {

    private final SedeAdminService sedeAdminService;

    public SedeAdminController(SedeAdminService sedeAdminService) {
        this.sedeAdminService = sedeAdminService;
    }

    @GetMapping("/{idSede}/servizi")
    public List<ServizioSedeResponse> getServiziPerSede(@PathVariable Integer idSede) {
        return sedeAdminService.getServiziPerSede(idSede);
    }

    @PostMapping("/{idSede}/servizi/{tipo}/{idServizio}")
    public void attivaServizioInSede(
            @PathVariable Integer idSede, @PathVariable String tipo, @PathVariable Integer idServizio) {
        sedeAdminService.attivaServizioInSede(idSede, tipo, idServizio);
    }

    @DeleteMapping("/{idSede}/servizi/{tipo}/{idServizio}")
    public void disattivaServizioInSede(
            @PathVariable Integer idSede, @PathVariable String tipo, @PathVariable Integer idServizio) {
        sedeAdminService.disattivaServizioInSede(idSede, tipo, idServizio);
    }
}