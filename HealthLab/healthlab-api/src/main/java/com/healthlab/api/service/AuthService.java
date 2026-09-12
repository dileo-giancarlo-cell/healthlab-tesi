package com.healthlab.api.service;

import com.healthlab.api.dto.request.LoginRequest;
import com.healthlab.api.dto.request.PasswordDimenticataRequest;
import com.healthlab.api.dto.request.RegistrazioneRequest;
import com.healthlab.api.dto.request.ResetPasswordRequest;
import com.healthlab.api.dto.request.SegretarioRegistraPazienteRequest;
import com.healthlab.api.dto.response.LoginResponse;
import com.healthlab.api.dto.response.MessaggioResponse;
import com.healthlab.api.dto.response.RegistrazioneResponse;
import com.healthlab.api.entity.Paziente;
import com.healthlab.api.entity.Utente;
import com.healthlab.api.repository.PazienteRepository;
import com.healthlab.api.repository.RuoloRepository;
import com.healthlab.api.repository.UtenteRepository;
import com.healthlab.api.security.JwtUtil;
import com.healthlab.api.dto.response.VerificaEmailResponse;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UtenteRepository utenteRepository;
    private final JwtUtil jwtUtil;
    private final RuoloRepository ruoloRepository;
    private final PazienteRepository pazienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthService(AuthenticationManager authenticationManager,
                        UtenteRepository utenteRepository,
                        JwtUtil jwtUtil, 
                        RuoloRepository ruoloRepository, 
                        PazienteRepository pazienteRepository, 
                        PasswordEncoder passwordEncoder, 
                        EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.utenteRepository = utenteRepository;
        this.jwtUtil = jwtUtil;
        this.ruoloRepository = ruoloRepository;
        this.pazienteRepository = pazienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public LoginResponse login(LoginRequest request) {
    authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
    );

    Utente utente = utenteRepository.findByUsername(request.getUsername())
            .orElseThrow();


    if (utente.isDisabilitato()) {
        throw new IllegalArgumentException("Questo account è stato disattivato. Contatta la struttura.");
    }

    if (!utente.isEmailVerificata()) {
        throw new IllegalArgumentException("Devi prima confermare la tua email. Controlla la tua casella di posta.");
    }

    String ruolo = utente.getRuolo().getNome();
    String token = jwtUtil.generateToken(utente.getUsername(), ruolo);

    return new LoginResponse(token, ruolo, utente.getId(), utente.getNome());
}

    public RegistrazioneResponse registra(RegistrazioneRequest request) {
        validaRobustezzaPassword(request.getPassword());

        var utenteEsistenteUsername = utenteRepository.findByUsername(request.getUsername());
        if (utenteEsistenteUsername.isPresent()) {
            if (!utenteEsistenteUsername.get().isEmailVerificata()) {
                throw new IllegalArgumentException("Registrazione già effettuata con questo nome utente: controlla la tua email per confermarla.");
            }
            throw new IllegalArgumentException("Nome utente già in uso.");
        }

        var utenteEsistenteEmail = utenteRepository.findByEmail(request.getEmail());
        if (utenteEsistenteEmail.isPresent()) {
            if (!utenteEsistenteEmail.get().isEmailVerificata()) {
                throw new IllegalArgumentException("Registrazione già effettuata con questa email: controlla la tua casella per confermarla.");
            }
            throw new IllegalArgumentException("Email già registrata.");
        }

        var ruoloPaziente = ruoloRepository.findByNome("paziente")
                .orElseThrow(() -> new IllegalStateException("Ruolo 'paziente' non configurato"));

        String token = UUID.randomUUID().toString();

        Utente nuovoUtente = new Utente();
        nuovoUtente.setNome(request.getNome());
        nuovoUtente.setCognome(request.getCognome());
        nuovoUtente.setUsername(request.getUsername());
        nuovoUtente.setEmail(request.getEmail());
        nuovoUtente.setTelefono(request.getTelefono());
        nuovoUtente.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        nuovoUtente.setCodiceFiscale(request.getCodiceFiscale());
        nuovoUtente.setDataNascita(request.getDataNascita());
        nuovoUtente.setRuolo(ruoloPaziente);
        nuovoUtente.setDataCreazione(LocalDateTime.now());
        nuovoUtente.setDisabilitato(false);
        nuovoUtente.setEmailVerificata(false);
        nuovoUtente.setTokenVerifica(token);
        nuovoUtente.setTokenScadenza(LocalDateTime.now().plusHours(24));

        Utente utenteSalvato = utenteRepository.save(nuovoUtente);

        Paziente paziente = new Paziente();
        paziente.setUtente(utenteSalvato);
        paziente.setSesso(Paziente.Sesso.valueOf(request.getSesso()));
        paziente.setConsensoTrattamentoDati(true);
        pazienteRepository.save(paziente);

        emailService.inviaEmailVerifica(utenteSalvato.getEmail(), utenteSalvato.getNome(), token);

        return new RegistrazioneResponse(
            "Registrazione completata con successo.",
            utenteSalvato.getEmail()
        );

    }

    // Registrazione di un paziente da parte della Segreteria
    // (es. richiesta telefonica o allo sportello). Differenze rispetto a
    // registra(): nessuna password scelta dall'utente (generata una
    // robusta e casuale, da cambiare al primo login).
    // Il "completamento" avviene riusando il meccanismo di reset password
    // scadenza più permissiva (7 giorni).
    public RegistrazioneResponse registraPerContoSegreteria(SegretarioRegistraPazienteRequest request) {
        if (utenteRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Nome utente già in uso.");
        }
        if (utenteRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email già registrata.");
        }

        var ruoloPaziente = ruoloRepository.findByNome("paziente")
                .orElseThrow(() -> new IllegalStateException("Ruolo 'paziente' non configurato"));

        String passwordCasuale = generaPasswordCasualeSicura();
        String tokenCompletamento = UUID.randomUUID().toString();

        Utente nuovoUtente = new Utente();
        nuovoUtente.setNome(request.getNome());
        nuovoUtente.setCognome(request.getCognome());
        nuovoUtente.setUsername(request.getUsername());
        nuovoUtente.setEmail(request.getEmail());
        nuovoUtente.setTelefono(request.getTelefono());
        nuovoUtente.setPasswordHash(passwordEncoder.encode(passwordCasuale));
        nuovoUtente.setCodiceFiscale(request.getCodiceFiscale());
        nuovoUtente.setDataNascita(request.getDataNascita());
        nuovoUtente.setRuolo(ruoloPaziente);
        nuovoUtente.setDataCreazione(LocalDateTime.now());
        nuovoUtente.setDisabilitato(false);
        nuovoUtente.setEmailVerificata(false);
        nuovoUtente.setTokenReset(tokenCompletamento);
        nuovoUtente.setTokenResetScadenza(LocalDateTime.now().plusDays(7));

        Utente utenteSalvato = utenteRepository.save(nuovoUtente);

        Paziente paziente = new Paziente();
        paziente.setUtente(utenteSalvato);
        paziente.setSesso(Paziente.Sesso.valueOf(request.getSesso()));
        paziente.setConsensoTrattamentoDati(true);
        pazienteRepository.save(paziente);

        emailService.inviaEmailInvitoRegistrazione(
                utenteSalvato.getEmail(), utenteSalvato.getNome(), tokenCompletamento);

        return new RegistrazioneResponse(
                "Paziente registrato con successo.",
                utenteSalvato.getEmail()
        );
    }

    // Stessi criteri mostrati come checklist in tempo reale nel frontend
    // (login.html, pannello di registrazione) — qui è la verifica che conta
    // davvero: senza questa, chiunque potesse chiamare l'API direttamente
    // (bypassando il sito) potrebbe registrarsi con una password di un
    // solo carattere, a prescindere da cosa mostri l'interfaccia.
    private void validaRobustezzaPassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("La password deve contenere almeno 8 caratteri.");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("La password deve contenere almeno una lettera maiuscola.");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("La password deve contenere almeno una lettera minuscola.");
        }
        if (!password.matches(".*[0-9].*")) {
            throw new IllegalArgumentException("La password deve contenere almeno un numero.");
        }
        if (!password.matches(".*[!@#$%^&*()\\-_=+\\[\\]{};:,.<>?/\\\\|`~].*")) {
            throw new IllegalArgumentException("La password deve contenere almeno un carattere speciale.");
        }
    }

    private String generaPasswordCasualeSicura() {
        String caratteri = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            sb.append(caratteri.charAt(random.nextInt(caratteri.length())));
        }
        return sb.toString();
    }

    public VerificaEmailResponse verificaEmail(String token) {
        Utente utente = utenteRepository.findByTokenVerifica(token)
                .orElse(null);

        if (utente == null) {
            return new VerificaEmailResponse(false, "Link di verifica non valido.");
        }
        if (utente.getTokenScadenza().isBefore(LocalDateTime.now())) {
            return new VerificaEmailResponse(false, "Il link di verifica è scaduto. Richiedine uno nuovo.");
        }

        utente.setEmailVerificata(true);
        utente.setTokenVerifica(null);
        utente.setTokenScadenza(null);
        utenteRepository.save(utente);

        return new VerificaEmailResponse(true, "Email confermata con successo! Ora puoi accedere.");
        
    }

    public MessaggioResponse richiediResetPassword(PasswordDimenticataRequest request) {
        // Evito di rivelare se l'email esiste o meno per motivi di sicurezza, quindi rispondo sempre con lo stesso messaggio.
        var utenteOpt = utenteRepository.findByEmail(request.getEmail());

        if (utenteOpt.isPresent()) {
            Utente utente = utenteOpt.get();
            String token = UUID.randomUUID().toString();
            utente.setTokenReset(token);
            utente.setTokenResetScadenza(LocalDateTime.now().plusHours(1));
            utenteRepository.save(utente);

            try {
                emailService.inviaEmailResetPassword(utente.getEmail(), utente.getNome(), token);
            } catch (Exception e) {
                System.err.println("Errore nell'invio email di reset password: " + e.getMessage());
            }
        }

        return new MessaggioResponse("Se l'indirizzo email è registrato, riceverai a breve un link per reimpostare la password.");
    }

    public MessaggioResponse resetPassword(ResetPasswordRequest request) {
        Utente utente = utenteRepository.findByTokenReset(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Link di reset non valido."));

        if (utente.getTokenResetScadenza().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Il link di reset è scaduto. Richiedine uno nuovo.");
        }

        utente.setPasswordHash(passwordEncoder.encode(request.getNuovaPassword()));
        utente.setTokenReset(null);
        utente.setTokenResetScadenza(null);
        utente.setEmailVerificata(true);
        utenteRepository.save(utente);

        return new MessaggioResponse("Password aggiornata con successo. Ora puoi accedere.");
    }

}