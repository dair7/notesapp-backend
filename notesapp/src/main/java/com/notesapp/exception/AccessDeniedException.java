package com.notesapp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String mensaje) {
        super(mensaje);
    }

    public AccessDeniedException() {
        super("No tienes permiso para acceder a este recurso");
    }
}
