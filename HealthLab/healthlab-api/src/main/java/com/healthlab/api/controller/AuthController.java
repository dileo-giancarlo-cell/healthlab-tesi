package com.healthlab.api.controller;

import com.healthlab.api.dto.request.LoginRequest;
import com.healthlab.api.dto.request.PasswordDimenticataRequest;
import com.healthlab.api.dto.request.RegistrazioneRequest;
import com.healthlab.api.dto.request.ResetPasswordRequest;
import com.healthlab.api.dto.response.LoginResponse;
import com.healthlab.api.dto.response.MessaggioResponse;
import com.healthlab.api.dto.response.RegistrazioneResponse;
import com.healthlab.api.dto.response.VerificaEmailResponse;
import com.healthlab.api.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/registrazione")
    public RegistrazioneResponse registra(@Valid @RequestBody RegistrazioneRequest request) {
        return authService.registra(request);
    }

    @GetMapping("/verifica")
    public VerificaEmailResponse verifica(@RequestParam String token) {
        return authService.verificaEmail(token);
    }

    @PostMapping("/password-dimenticata")
    public MessaggioResponse passwordDimenticata(@Valid @RequestBody PasswordDimenticataRequest request) {
        return authService.richiediResetPassword(request);
    }

    @PostMapping("/reset-password")
    public MessaggioResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }
}