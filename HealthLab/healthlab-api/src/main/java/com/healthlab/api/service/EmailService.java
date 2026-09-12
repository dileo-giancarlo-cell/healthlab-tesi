package com.healthlab.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mittente;

    @Value("${healthlab.frontend-url}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void inviaEmailVerifica(String destinatario, String nome, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mittente);
        message.setTo(destinatario);
        message.setSubject("HealthLab — Conferma la tua email");
        message.setText(
                "Ciao " + nome + ",\n\n" +
                "grazie per esserti registrato su HealthLab. Conferma il tuo indirizzo email cliccando sul link seguente:\n\n" +
                frontendUrl + "/verifica-email.html?token=" + token + "\n\n" +
                "Il link è valido per 24 ore.\n\n" +
                "Il team HealthLab"
        );
        mailSender.send(message);
    }

    public void inviaEmailResetPassword(String destinatario, String nome, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mittente);
        message.setTo(destinatario);
        message.setSubject("HealthLab — Reimposta la tua password");
        message.setText(
                "Ciao " + nome + ",\n\n" +
                "hai richiesto di reimpostare la password del tuo account HealthLab. Clicca sul link seguente per sceglierne una nuova:\n\n" +
                frontendUrl + "/reset-password.html?token=" + token + "\n\n" +
                "Se non hai richiesto tu questa operazione, ignora pure questa email: la tua password resterà invariata.\n" +
                "Il link è valido per 1 ora.\n\n" +
                "Il team HealthLab"
        );
        mailSender.send(message);
    }

    // Inviata quando la Segreteria registra un paziente (es. telefono
    // o sportello): riusa lo stesso link di reset-password.html come "completa la
    // registrazione". A differenza del reset password normale, qui non c'è nessuna urgenza (il
    // token dura 7 giorni, non 1 ora): l'appuntamento del paziente è comunque
    // confermato a prescindere da quando/se completerà la registrazione.
    public void inviaEmailInvitoRegistrazione(String destinatario, String nome, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mittente);
        message.setTo(destinatario);
        message.setSubject("HealthLab — Completa la registrazione del tuo account");
        message.setText(
                "Ciao " + nome + ",\n\n" +
                "la segreteria di HealthLab ha creato il tuo account. Per accedere online alle tue prenotazioni e ai tuoi referti, completa la registrazione impostando una password personale:\n\n" +
                frontendUrl + "/reset-password.html?token=" + token + "\n\n" +
                "Se preferisci non completare subito la registrazione, nessun problema: eventuali appuntamenti restano comunque validi.\n" +
                "Il link è valido per 7 giorni — se scade, puoi richiederne uno nuovo dalla pagina \"Password dimenticata\":\n" +
                frontendUrl + "/password-dimenticata.html\n\n" +
                "Il team HealthLab"
        );
        mailSender.send(message);
    }

    // Inviata a ogni prenotazione confermata (dal paziente stesso o per suo
    // conto dalla Segreteria), a prescindere da chi prenota e dallo stato di
    // registrazione del paziente — per un paziente non ancora registrato è
    // l'unico modo per sapere che ha un appuntamento.
    // nomeMedico e parametriSelezionati sono opzionali: nulli/vuoti quando non
    // pertinenti (es. prestazione eseguita da un tecnico, nessun parametro scelto).
    public void inviaEmailConfermaPrenotazione(String destinatario, String nomePaziente, String nomeServizio,
                                                LocalDateTime dataOra, String nomeSede, String indirizzoSede,
                                                String nomeMedico, List<String> parametriSelezionati) {
        DateTimeFormatter formatterData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formatterOra = DateTimeFormatter.ofPattern("HH:mm");

        StringBuilder testo = new StringBuilder();
        testo.append("Ciao ").append(nomePaziente).append(",\n\n");
        testo.append("la tua prenotazione è confermata:\n\n");
        testo.append("Servizio: ").append(nomeServizio).append("\n");
        testo.append("Data e ora: ").append(dataOra.format(formatterData))
                .append(" alle ").append(dataOra.format(formatterOra)).append("\n");

        if (nomeSede != null) {
            testo.append("Sede: ").append(nomeSede);
            if (indirizzoSede != null && !indirizzoSede.isBlank()) {
                testo.append(" — ").append(indirizzoSede);
            }
            testo.append("\n");
        }

        if (nomeMedico != null) {
            testo.append("A cura di: Dott. ").append(nomeMedico).append("\n");
        }

        if (parametriSelezionati != null && !parametriSelezionati.isEmpty()) {
            testo.append("Parametri richiesti: ").append(String.join(", ", parametriSelezionati)).append("\n");
        }

        testo.append("\nPuoi consultare e gestire le tue prenotazioni nella tua area riservata su HealthLab:\n");
        testo.append(frontendUrl).append("/le-mie-prenotazioni.html\n\n");
        testo.append("Il team HealthLab");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mittente);
        message.setTo(destinatario);
        message.setSubject("HealthLab — Prenotazione confermata");
        message.setText(testo.toString());
        mailSender.send(message);
    }
}