package com.healthlab.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${healthlab.referti.storage-path}")
    private String storagePath;

    @Value("${healthlab.categorie.storage-path}")
    private String categorieStoragePath;

    public String salvaPdf(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Il file è vuoto.");
        }
        if (!"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("Sono ammessi solo file PDF.");
        }

        try {
            Path directory = Paths.get(storagePath);
            Files.createDirectories(directory);

            // Nome file univoco, per evitare sovrascritture tra referti diversi
            String nomeFile = UUID.randomUUID() + ".pdf";
            Path destinazione = directory.resolve(nomeFile);
            file.transferTo(destinazione);

            return nomeFile;   // salviamo solo il nome, non il path assoluto

        } catch (IOException e) {
            throw new RuntimeException("Errore durante il salvataggio del file.", e);
        }
    }

    public Path getPercorsoCompleto(String nomeFile) {
        return Paths.get(storagePath).resolve(nomeFile).normalize();
    }

    // A differenza dei referti (nome sempre univoco via UUID, mai riferito da
    // fuori se non tramite il database), qui il nome è VOLUTAMENTE
    // deterministico — passato dal chiamante, già standardizzato (es.
    // "home-carousel-analisi"). Ricaricare l'immagine per la stessa categoria
    // sovrascrive semplicemente quella precedente, niente file orfani ad
    // accumularsi ad ogni modifica.
    public String salvaImmagineCategoria(MultipartFile file, String nomeBaseStandardizzato) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Il file è vuoto.");
        }

        String estensione = switch (file.getContentType() == null ? "" : file.getContentType()) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new IllegalArgumentException(
                    "Formato immagine non supportato. Usa JPG, PNG o WEBP.");
        };

        try {
            Path directory = Paths.get(categorieStoragePath);
            Files.createDirectories(directory);

            String nomeFile = nomeBaseStandardizzato + estensione;
            Path destinazione = directory.resolve(nomeFile);

            // REPLACE_EXISTING esplicito: a differenza di transferTo() usato per
            // i referti, qui vogliamo la sovrascrittura garantita, non un errore
            // se il file esiste già .
            Files.copy(file.getInputStream(), destinazione, StandardCopyOption.REPLACE_EXISTING);

            return nomeFile;

        } catch (IOException e) {
            throw new RuntimeException("Errore durante il salvataggio dell'immagine.", e);
        }
    }

    public Path getPercorsoCompletoImmagineCategoria(String nomeFile) {
        return Paths.get(categorieStoragePath).resolve(nomeFile).normalize();
    }
}