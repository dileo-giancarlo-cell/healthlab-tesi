package com.healthlab.api.exception;

public class AutorizzazioneNonPresenteException extends RuntimeException {
    public AutorizzazioneNonPresenteException(String message) {
        super(message);
    }
}