package com.indramind.cybersec.secure_tasks_api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse(LocalDateTime.now(), ex.getMessage())
        );
    }

    @ExceptionHandler(EmailInUseException.class)
    public ResponseEntity<ErrorResponse> handleEmailInUse(EmailInUseException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            new ErrorResponse(LocalDateTime.now(), ex.getMessage())
        );
    }

    @ExceptionHandler(CollaboratorAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleCollaboratorAlreadyExists(CollaboratorAlreadyExistsException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ErrorResponse(LocalDateTime.now(), ex.getMessage())
        );
    }

    @ExceptionHandler(CollaboratorNotFound.class)
    public ResponseEntity<ErrorResponse> handleCollaboratorAlreadyExists(CollaboratorNotFound ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse(LocalDateTime.now(), ex.getMessage())
        );
    }

    // Incorrect requests catched by @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {

        String errorMessage = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(e -> "[" + e.getField() + "] " + e.getDefaultMessage())
            .reduce((a, b) -> a + " | " + b)
            .orElse("Validation error");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new ErrorResponse(LocalDateTime.now(), errorMessage)
        );
    }

	@ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse(LocalDateTime.now(), "Internal server error")
        );
    }
}