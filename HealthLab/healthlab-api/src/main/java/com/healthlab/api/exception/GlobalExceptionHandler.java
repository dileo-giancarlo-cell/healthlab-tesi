package com.healthlab.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AutorizzazioneNonPresenteException.class)
    public ResponseEntity<Map<String, String>> handleAutorizzazioneNonPresente(
            AutorizzazioneNonPresenteException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)   // 409
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)   // 400
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(ConflittoPrenotazioneException.class)
    public ResponseEntity<Map<String, String>> handleConflittoPrenotazione(ConflittoPrenotazioneException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("message", ex.getMessage()));
    }
}