package com.parking.parkspot.service.exception;

import com.parking.parkspot.payload.response.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        return ResponseEntity.badRequest().body(new MessageResponse("Error: " + ex.getMessage()));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<?> handleDuplicateResourceException(DuplicateResourceException ex, WebRequest request) {
        return ResponseEntity.badRequest().body(new MessageResponse("Error: " + ex.getMessage()));
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<?> handleInvalidOperationException(InvalidOperationException ex, WebRequest request) {
        return ResponseEntity.badRequest().body(new MessageResponse("Error: " + ex.getMessage()));
    }

    @ExceptionHandler(DatosInconsistentesException.class)
    public ResponseEntity<?> handleDatosInconsistentesException(DatosInconsistentesException ex, WebRequest request) {
        return ResponseEntity.badRequest().body(new MessageResponse("Error: " + ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return ResponseEntity.badRequest().body(new MessageResponse("Error: " + ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex, WebRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Error interno del servidor: " + ex.getMessage()));
    }
}
