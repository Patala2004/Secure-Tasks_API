package com.indramind.cybersec.secure_tasks_api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of(
                        "timestamp", LocalDateTime.now(),
                        "error", ex.getMessage()
                )
        );
    }

    @ExceptionHandler(EmailInUseException.class)
    public ResponseEntity<?> handleEmailInUse(EmailInUseException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            Map.of(
                        "timestamp", LocalDateTime.now(),
                        "error", ex.getMessage()
            )
        );
    }

	@ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Map.of("error", "Internal server error")
        );
    }
}