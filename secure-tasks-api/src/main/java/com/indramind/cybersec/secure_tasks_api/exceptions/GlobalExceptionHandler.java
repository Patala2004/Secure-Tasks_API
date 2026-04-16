package com.indramind.cybersec.secure_tasks_api.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.*;

import com.indramind.cybersec.secure_tasks_api.security.CorrelationIdFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {

        if (log.isWarnEnabled()) log.warn("Resource not found: message={}, correlationId={}",
            ex.getMessage(),
            MDC.get(CorrelationIdFilter.CORRELATION_KEY));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse(LocalDateTime.now(), ex.getMessage())
        );
    }

    @ExceptionHandler(EmailInUseException.class)
    public ResponseEntity<ErrorResponse> handleEmailInUse(EmailInUseException ex){

        if (log.isWarnEnabled()) log.warn("Email already in use: message={}, correlationId={}",
            ex.getMessage(),
            MDC.get(CorrelationIdFilter.CORRELATION_KEY));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            new ErrorResponse(LocalDateTime.now(), ex.getMessage())
        );
    }

    @ExceptionHandler(CollaboratorAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleCollaboratorAlreadyExists(CollaboratorAlreadyExistsException ex){

        if (log.isWarnEnabled()) log.warn("Collaborator already exists: message={}, correlationId={}",
            ex.getMessage(),
            MDC.get(CorrelationIdFilter.CORRELATION_KEY));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ErrorResponse(LocalDateTime.now(), ex.getMessage())
        );
    }

    @ExceptionHandler(CollaboratorNotFound.class)
    public ResponseEntity<ErrorResponse> handleCollaboratorAlreadyExists(CollaboratorNotFound ex){

        if (log.isWarnEnabled()) log.warn("Collaborator not found: message={}, correlationId={}",
            ex.getMessage(),
            MDC.get(CorrelationIdFilter.CORRELATION_KEY));

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
        
        if (log.isWarnEnabled()) log.warn("Validation failed: errors={}, correlationId={}",
            errorMessage,
            MDC.get(CorrelationIdFilter.CORRELATION_KEY));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new ErrorResponse(LocalDateTime.now(), errorMessage)
        );
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex) {

        if (log.isWarnEnabled()) log.warn(
            "Missing request header: {} correlationId={}",
            ex.getHeaderName(),
            MDC.get(CorrelationIdFilter.CORRELATION_KEY)
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new ErrorResponse(LocalDateTime.now(), "Missing required header: " + ex.getHeaderName())
        );
    }

	@ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {

        if (log.isErrorEnabled()) log.error("Unhandled exception: type={}, message={}, correlationId={}",
            ex.getClass().getSimpleName(),
            ex.getMessage(),
            MDC.get(CorrelationIdFilter.CORRELATION_KEY),
            ex);


        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse(LocalDateTime.now(), "Internal server error")
        );
    }
}